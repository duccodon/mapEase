package com.example.mapease;

import static java.lang.Thread.sleep;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mapease.Remote.RouteData;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapease.Remote.RoutesAPIHelper;
import com.example.mapease.Remote.Step;
import com.example.mapease.Utils.SlidingPanelHelper;
import com.example.mapease.events.SendLocationToActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mapease.databinding.ActivityRouteBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.maps.android.PolyUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.CompositeDisposable;

public class RouteActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final double LOCATION_THRESHOLD = 0.0001; // Adjust based on accuracy needs
    private GoogleMap mMap;
    private ActivityRouteBinding binding;
    private SendLocationToActivity sendLocationToActivity;

    //Routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Polyline blackPolyline;
    private com.google.android.gms.maps.model.Polyline greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;
    private Marker originMarker, destinationMarker;
    private AutocompleteSupportFragment autocompleteOrigin, autocompleteDestination;
    private LatLng currentLocation;
    private SlidingUpPanelLayout slidingLayout;
    private LinearLayout slidingPanel;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RoutePagerAdapter pagerAdapter;

    private List<Step> stepList =  new ArrayList<>();
    private List<RouteData> routeDataList = new ArrayList<>();
    private int completedRequests = 0;
    private int totalRequests = 3; // Number of times you call parseJsonWithMode
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
        if(EventBus.getDefault().hasSubscriberForEvent(SendLocationToActivity.class)){
            EventBus.getDefault().removeStickyEvent(SendLocationToActivity.class);
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setSendLocationToActivity(SendLocationToActivity event){
        sendLocationToActivity = event;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRouteBinding.inflate(getLayoutInflater());

        setContentView(R.layout.activity_route);

        //setup sliding panel
        slidingLayout = findViewById(R.id.sliding_layout);
        slidingPanel = findViewById(R.id.route_info_sliding_panel);
        SlidingPanelHelper.setupPanel(this, slidingLayout, slidingPanel);

        //Initialize Tab Layout using ViewPager2
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Setup autocomplete
        setupAutocomplete();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.map_container, mapFragment, "MAP_FRAGMENT")
                .commit();
        mapFragment.getMapAsync(this);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng Activity hiện tại
            }
        });
    }
    @Override
    protected void onDestroy() {
        if (mMap != null) {
            mMap.clear();  // Xóa tất cả đối tượng khỏi bản đồ
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);  // Giải phóng tài nguyên
        }
        super.onDestroy();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            return;
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(0, 200, 0, 420); // Dịch chuyển nút Zoom lên trên 150px
        if (sendLocationToActivity != null) {
            loadRouteData(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination());

            currentLocation = sendLocationToActivity.getOrigin();

            mMap.setOnMyLocationButtonClickListener(() -> {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sendLocationToActivity.getOrigin(), 18.0f));
                return true;
            });

            setAutocompleteText(R.id.autocomplete_origin, "Your Location");
            setAutocompleteText(R.id.autocomplete_destination, sendLocationToActivity.getDestinationName());

        } else {
            Toast.makeText(this, "Route data not available yet", Toast.LENGTH_SHORT).show();
        }

        View locationButton = ((View)findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.setMargins(0, 0, 0, 300);
    }

    private void parseJsonWithMode(LatLng origin, LatLng destination, String mode, OnRouteDataLoadedListener listener) throws JSONException
    {
        if (origin == null || destination == null) {
            Toast.makeText(this, "Origin or Destination is null", Toast.LENGTH_SHORT).show();

        }

        Log.d("RouteActivity", "Requesting path: " + origin.toString() + " to " + destination.toString());
        mMap.clear();
        // Gửi request và nhận `JSONObject`
        RoutesAPIHelper.requestRoute(this, origin.latitude, origin.longitude, destination.latitude, destination.longitude, "*", mode, response -> {
            try {
                Log.d("API_RETURN", "Response JSON: " + response.toString());
                // Lưu lại response JSON (ví dụ có thể lưu vào biến toàn cục hoặc xử lý tiếp)
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray routesArray = jsonObject.getJSONArray("routes");

                if (routesArray.length() == 0) {
                    Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show();
                }

                if (routesArray.length() == 0) {
                    Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (polylineList == null) {
                    polylineList = new ArrayList<>();
                }
                polylineList.clear();

                // Lấy tuyến đường đầu tiên
                JSONObject route = routesArray.getJSONObject(0);
                JSONArray legs = route.getJSONArray("legs");

                for (int i = 0; i < legs.length(); i++) {
                    JSONObject leg = legs.getJSONObject(i);
                    JSONArray steps = leg.getJSONArray("steps");

                    stepList.clear();  // This will clear previous steps
                    polylineList.clear(); // This will clear previous polyline

                    for (int j = 0; j < steps.length(); j++) {
                        JSONObject step = steps.getJSONObject(j);

                        // Lấy polyline từ từng step
                        JSONObject polyline = step.getJSONObject("polyline");
                        String encodedPolyline = polyline.getString("encodedPolyline");

                        JSONObject navigationInstruction = step.optJSONObject("navigationInstruction");
                        String maneuver = "";
                        String instruction = "";

                        // Only extract maneuver and instruction if "navigationInstruction" is available
                        if (navigationInstruction != null) {
                            maneuver = navigationInstruction.optString("maneuver", ""); // Default to empty string if not found
                            instruction = navigationInstruction.optString("instructions", "");
                        }


                        int distanceMeters = step.getInt("distanceMeters");

                        // Tạo Step Object và thêm vào list
                        Step stepObject = new Step(maneuver, instruction, distanceMeters);
                        stepList.add(stepObject);

                        // Giải mã polyline va luu
                        List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
                        polylineList.addAll(decodedPath);
                    }
                }

                if (polylineList.isEmpty()) {
                    Toast.makeText(this, "No polyline data", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject legInfo = legs.getJSONObject(0);
                JSONObject duration = legInfo.getJSONObject("localizedValues").getJSONObject("duration");
                JSONObject distance = legInfo.getJSONObject("localizedValues").getJSONObject("distance");

                String durationText = duration.getString("text");
                String distanceText = distance.getString("text");

                RouteData routeData = new RouteData(durationText, distanceText, stepList, polylineList);

                routeDataList.add(routeData);
                Log.d("UPDATE ROUTE LIST", "add route to list " +  routeDataList.size());


                // 🔥 Call the callback here
                if (listener != null) {
                    listener.onRouteDataLoaded(routeDataList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
            }
            // Notify that this call is done
        });
    }

    private void drawPolylineOnMap(LatLng origin, LatLng destination, String mode) {
        List<LatLng> polylineList = new ArrayList<>();
        if(mode == "DRIVE")
        {
            polylineList = routeDataList.get(0).getPolylineList();
        }
        else if (mode == "RIDE")
        {
            polylineList = routeDataList.get(1).getPolylineList();
        }
        else if (mode == "WALK")
        {
            polylineList = routeDataList.get(2).getPolylineList();
        }
        Log.d("Polyline List","Polyline list length: " +  polylineList.size());
        mMap.clear();
        // Vẽ Polyline trên bản đồ
        polylineOptions = new PolylineOptions()
                .color(Color.GRAY)
                .width(12)
                .startCap(new com.google.android.gms.maps.model.SquareCap())
                .jointType(JointType.ROUND)
                .addAll(polylineList);

        greyPolyline = mMap.addPolyline(polylineOptions);

        blackPolylineOptions = new PolylineOptions()
                .color(Color.BLACK)
                .width(5)
                .startCap(new com.google.android.gms.maps.model.SquareCap())
                .jointType(JointType.ROUND)
                .addAll(polylineList);

        blackPolyline = mMap.addPolyline(blackPolylineOptions);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(4);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(value -> {
            List<LatLng> points = greyPolyline.getPoints();
            addOriginMarker(points);
            int percentValue = (int) value.getAnimatedValue();
            int size = points.size();
            int newPoints = (int) (size * (percentValue / 100.0f));
            List<LatLng> p = points.subList(0, newPoints);
            blackPolyline.setPoints(p);
        });
        valueAnimator.start();

        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(origin)
                .include(destination)
                .build();

        addDestinationMarker();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));
    }
    private void drawPath(LatLng origin, LatLng destination) throws JSONException {
        if (origin == null || destination == null) {
            Toast.makeText(this, "Origin or Destination is null", Toast.LENGTH_SHORT).show();
            return;
        }
        clearPreviousRoutes();

        Log.d("RouteActivity", "Requesting path: " + origin.toString() + " to " + destination.toString());
        mMap.clear();
        // Gửi request và nhận `JSONObject`
        RoutesAPIHelper.requestRoute(this, origin.latitude, origin.longitude, destination.latitude, destination.longitude, "*", "DRIVE", response -> {
            try {
                Log.d("API_RETURN", "Response JSON: " + response.toString());
                saveJsonToExternalStorage(response.toString(), "api_response.txt");
                // Lưu lại response JSON (ví dụ có thể lưu vào biến toàn cục hoặc xử lý tiếp)
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray routesArray = jsonObject.getJSONArray("routes");

                if (routesArray.length() == 0) {
                        Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show();
                    }

                if (routesArray.length() == 0) {
                    Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (polylineList == null) {
                    polylineList = new ArrayList<>();
                }
                polylineList.clear();

                // Lấy tuyến đường đầu tiên
                JSONObject route = routesArray.getJSONObject(0);
                JSONArray legs = route.getJSONArray("legs");

                for (int i = 0; i < legs.length(); i++) {
                    JSONObject leg = legs.getJSONObject(i);
                    JSONArray steps = leg.getJSONArray("steps");

                    stepList.clear();  // This will clear previous steps

                    for (int j = 0; j < steps.length(); j++) {
                        JSONObject step = steps.getJSONObject(j);

                        // Lấy polyline từ từng step
                        JSONObject polyline = step.getJSONObject("polyline");
                        String encodedPolyline = polyline.getString("encodedPolyline");

                        JSONObject navigationInstruction = step.optJSONObject("navigationInstruction");
                        String maneuver = "";
                        String instruction = "";

                        // Only extract maneuver and instruction if "navigationInstruction" is available
                        if (navigationInstruction != null) {
                            maneuver = navigationInstruction.optString("maneuver", ""); // Default to empty string if not found
                            instruction = navigationInstruction.optString("instructions", "");
                        }


                        int distanceMeters = step.getInt("distanceMeters");

                        // Tạo Step Object và thêm vào list
                        Step stepObject = new Step(maneuver, instruction, distanceMeters);
                        Log.d("STEP BY STEP", stepObject.toString());

                        stepList.add(stepObject);


                        // Giải mã polyline
                        List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
                        polylineList.addAll(decodedPath);
                    }

                }
                if (polylineList.isEmpty()) {
                    Toast.makeText(this, "No polyline data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vẽ Polyline trên bản đồ
                polylineOptions = new PolylineOptions()
                        .color(Color.GRAY)
                        .width(12)
                        .startCap(new com.google.android.gms.maps.model.SquareCap())
                        .jointType(JointType.ROUND)
                        .addAll(polylineList);

                greyPolyline = mMap.addPolyline(polylineOptions);

                blackPolylineOptions = new PolylineOptions()
                        .color(Color.BLACK)
                        .width(5)
                        .startCap(new com.google.android.gms.maps.model.SquareCap())
                        .jointType(JointType.ROUND)
                        .addAll(polylineList);

                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                valueAnimator.setDuration(1);
                valueAnimator.setRepeatCount(3);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(value -> {
                    List<LatLng> points = greyPolyline.getPoints();
                    addOriginMarker(points);
                    int percentValue = (int) value.getAnimatedValue();
                    int size = points.size();
                    int newPoints = (int) (size * (percentValue / 100.0f));
                    List<LatLng> p = points.subList(0, newPoints);
                    blackPolyline.setPoints(p);
                });
                valueAnimator.start();

                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                        .include(origin)
                        .include(destination)
                        .build();

                // Hiển thị thông tin khoảng cách & thời gian
                JSONObject legInfo = legs.getJSONObject(0);
                JSONObject duration = legInfo.getJSONObject("localizedValues").getJSONObject("duration");
                JSONObject distance = legInfo.getJSONObject("localizedValues").getJSONObject("distance");

                String durationText = duration.getString("text");
                String distanceText = distance.getString("text");
/*
                // Lấy vị trí bắt đầu/kết thúc từ JSON của Route API
                JSONObject startLocation = legInfo.getJSONObject("startLocation").getJSONObject("latLng");
                JSONObject endLocation = legInfo.getJSONObject("endLocation").getJSONObject("latLng");

                String startLatLng = startLocation.getDouble("latitude") + "," + startLocation.getDouble("longitude");
                String endLatLng = endLocation.getDouble("latitude") + "," + endLocation.getDouble("longitude");*/
                addDestinationMarker();

                TextView tvDurationDistance = findViewById(R.id.tv_duration_distance);
                tvDurationDistance.setText(durationText + " (" + distanceText + ")");

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupAutocomplete() {
        Places.initialize(this, getString(R.string.ggMapAPIKey));
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        AutocompleteSupportFragment autocompleteOrigin = AutocompleteSupportFragment.newInstance();
        AutocompleteSupportFragment autocompleteDestination = AutocompleteSupportFragment.newInstance();

        transaction.replace(R.id.autocomplete_origin, autocompleteOrigin, "autocomplete_origin");
        transaction.replace(R.id.autocomplete_destination, autocompleteDestination, "autocomplete_destination");
        transaction.commit();

        autocompleteOrigin.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d("Autocomplete", "Origin: " + place.getDisplayName() + "Origin: " + place.getDisplayName());
                LatLng newLocation = place.getLatLng();
                if (newLocation != null) {
                    sendLocationToActivity.setOrigin(newLocation, place.getDisplayName());
                    // Update route only when both points are available
                    updateRoute(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination());
                }

            }
            @Override
            public void onError(@NonNull Status status) {
                Log.e("Autocomplete", "Error selecting origin: " + status);
            }
        });

        autocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d("Autocomplete", "Destination: " + place.getDisplayName());
                LatLng newLocation = place.getLatLng();
                if (newLocation != null) {
                    sendLocationToActivity.setDestination(newLocation, place.getDisplayName());
                    // Update route only when both points are available
                    updateRoute(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination());
                }
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.e("Autocomplete", "Error selecting destination: " + status);
            }
        });
    }

    /**
     * Cập nhật lại tuyến đường khi chọn điểm đi hoặc điểm đến
     */
    private void updateRoute(LatLng newOrigin, LatLng newDestination) {
        if (newOrigin != null && newDestination != null) {
/*            try {
                drawPath(newOrigin, newDestination);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            loadRouteData(newOrigin, newDestination);
        }
    }
    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            Log.e("MarkerError", "Vector drawable không hợp lệ");
            return null;
        }

        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();

        if (width <= 0 || height <= 0) {
            width = 100; // Đặt kích thước mặc định nếu cần
            height = 100;
        }

        // Tạo Bitmap hợp lệ
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            Log.e("MarkerError", "Không thể tạo Bitmap");
            return null;
        }

        // Truyền bitmap vào Canvas (cách đúng)
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addDestinationMarker() {
        BitmapDescriptor originIcon = getBitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_destination_marker);
        LatLng destinationLatLng = sendLocationToActivity.getDestination();
        if (originIcon != null) {
            originMarker = mMap.addMarker(new MarkerOptions()
                    .icon(originIcon)
                    .position(destinationLatLng));
        } else {
            Log.e("MarkerError", "Không thể tải icon cho marker");
        }
    }

    private void addOriginMarker(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty()) {
            Log.e("MarkerError", "Danh sách điểm tuyến đường rỗng");
            return;
        }

        // Lấy điểm đầu tiên của đoạn đường
        LatLng startOfRoute = routePoints.get(0);

        // Tạo icon vòng tròn trắng
        BitmapDescriptor originIcon = getBitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_origin_marker);

        if (originIcon != null) {
            originMarker = mMap.addMarker(new MarkerOptions()
                    .icon(originIcon)
                    .position(startOfRoute));
        } else {
            Log.e("MarkerError", "Không thể tải icon cho marker");
        }
    }

    private void setAutocompleteText(int fragmentId, String text) {
        new Handler(Looper.getMainLooper()).post(() -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(fragmentId);

            if (fragment instanceof AutocompleteSupportFragment) {
                ((AutocompleteSupportFragment) fragment).setText(text);
            } else {
                Log.e("Autocomplete", "Fragment not found or not an AutocompleteSupportFragment!");
            }
        });
    }
    private void clearPreviousRoutes() {
        if (blackPolyline != null) {
            blackPolyline.remove();
            blackPolyline = null;
        }
        if (greyPolyline != null) {
            greyPolyline.remove();
            greyPolyline = null;
        }
        if (polylineList != null) {
            polylineList.clear();
        }
    }
    public void swapLocations(View view) {
        // Lấy dữ liệu vị trí hiện tại
        LatLng currentOrigin = sendLocationToActivity.getOrigin();
        LatLng currentDestination = sendLocationToActivity.getDestination();
        String originName = sendLocationToActivity.getOriginName();
        String destinationName = sendLocationToActivity.getDestinationName();

        // Kiểm tra nếu cả hai vị trí đều hợp lệ
        if (currentOrigin != null && currentDestination != null) {
            // Hoán đổi vị trí
            sendLocationToActivity.setOrigin(currentDestination, destinationName);
            sendLocationToActivity.setDestination(currentOrigin, originName);

            // Cập nhật giao diện (nếu có ô nhập liệu hiển thị địa chỉ)
            String origin_text = sendLocationToActivity.getOriginName();
            String destination_text = sendLocationToActivity.getDestinationName();

            if (isCurrentLocation(sendLocationToActivity.getOrigin()))
            {
                origin_text = "Your Location";
            }
            else if (isCurrentLocation(sendLocationToActivity.getDestination()))
            {
                destination_text = "Your Location";
            }

            setAutocompleteText(R.id.autocomplete_origin, origin_text);
            setAutocompleteText(R.id.autocomplete_destination, destination_text);

            // Cập nhật tuyến đường
            updateRoute(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination());
        } else {
            Toast.makeText(this, "Please select both locations first", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isCurrentLocation(LatLng location) {

        if (location == null || currentLocation == null) {
            return false;
        }
        // Compare latitude and longitude with a small threshold
        return Math.abs(location.latitude - currentLocation.latitude) < LOCATION_THRESHOLD &&
                Math.abs(location.longitude - currentLocation.longitude) < LOCATION_THRESHOLD;
    }

    private void saveJsonToExternalStorage(String jsonResponse, String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

        try {
            // Pretty-print JSON
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String formattedJson = jsonObject.toString(4);

            // Write JSON to file
            try (FileWriter fileWriter = new FileWriter(file, false)) { // Overwrite
                fileWriter.write(formattedJson);
                fileWriter.flush();
                Log.d("FILE_SAVE", "Formatted JSON saved to: " + file.getAbsolutePath());
            }
        } catch (JSONException | IOException e) {
            Log.e("FILE_SAVE_ERROR", "Failed to save JSON", e);
        }
    }

    private void loadRouteData(LatLng origin, LatLng destination) //load route data for 3 mode, draw path for DRIVE mode
    {
        if (origin == null || destination == null) {
            Log.d("loadRouteData", "Load Route Data fail");
            return;
        }

        final int totalModes = 3;
        final AtomicInteger completedCount = new AtomicInteger(0);  // To track how many finished
        routeDataList.clear();  // Clear old data if needed

        OnRouteDataLoadedListener listener = updatedList -> {
            if (completedCount.incrementAndGet() == totalModes) {
                // 🎯 All 3 have finished!
                Log.d("loadRouteData", "All routes loaded. Total: " + routeDataList.size());

                Log.d("NUMBER OF ROUTE", "number of route: " + routeDataList.size());
                pagerAdapter = new RoutePagerAdapter(this, routeDataList);
                viewPager.setAdapter(pagerAdapter);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Drive");
                            tab.setIcon(R.drawable.ic_drive); // Thêm icon cho tab Drive
                            break;
                        case 1:
                            tab.setText("Ride");
                            tab.setIcon(R.drawable.ic_ride); // Thêm icon cho tab Ride
                            break;
                        case 2:
                            tab.setText("Walk");
                            tab.setIcon(R.drawable.ic_walk); // Thêm icon cho tab Walk
                            break;
                    }
                }).attach();
                drawPolylineOnMap(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination(), "DRIVE");

                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        // Xử lý khi tab được chọn
                        Log.d("TAB_SELECTED", "Selected tab position: " + position);

                        // Ví dụ: hiển thị thông tin tương ứng với tab
                        switch (position) {
                            case 0:
                                drawPolylineOnMap(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination(), "DRIVE");
                                break;
                            case 1:
                                drawPolylineOnMap(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination(), "RIDE");
                                break;
                            case 2:
                                drawPolylineOnMap(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination(), "WALK");
                                break;
                        }
                    }
                });

            }
        };

        try {
            parseJsonWithMode(origin, destination, "DRIVE", listener);
            parseJsonWithMode(origin, destination, "TWO_WHEELER", listener);
            parseJsonWithMode(origin, destination, "WALK", listener);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public interface OnRouteDataLoadedListener {
        void onRouteDataLoaded(List<RouteData> routeDataList);
    }

}