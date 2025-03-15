package com.example.mapease;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

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
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapease.Remote.RoutesAPIHelper;
import com.example.mapease.events.SendLocationToActivity;
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
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.PolyUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;

public class RouteActivity extends FragmentActivity implements OnMapReadyCallback {

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
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.ggMapAPIKey));
        }
        PlacesClient placesClient = Places.createClient(this);

        init();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Thêm SupportMapFragment động
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
                // Quay về MainActivity
                finish(); // Đóng Activity hiện tại
            }
        });

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
    private void init() {
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setPadding(0, 200, 0, 420); // Dịch chuyển nút Zoom lên trên 150px
        if (sendLocationToActivity != null) {
            mMap.setOnMyLocationButtonClickListener(() -> {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sendLocationToActivity.getOrigin(), 18.0f));
                return true;
            });
            // Vẽ đường đi
            try {
                drawPath(sendLocationToActivity.getOrigin(), sendLocationToActivity.getDestination());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(this, "Route data not available yet", Toast.LENGTH_SHORT).show();
        }

        View locationButton = ((View)findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.setMargins(0, 0, 0, 300);
    }

    private void drawPath(LatLng origin, LatLng destination) throws JSONException {
        if (origin == null || destination == null) {
            Toast.makeText(this, "Origin or Destination is null", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("RouteActivity", "Requesting path: " + origin.toString() + " to " + destination.toString());

        // Gửi request và nhận `JSONObject`
        RoutesAPIHelper.requestRoute(this, origin.latitude, origin.longitude, destination.latitude, destination.longitude, response -> {
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

                    for (int j = 0; j < steps.length(); j++) {
                        JSONObject step = steps.getJSONObject(j);

                        // Lấy polyline từ từng step
                        JSONObject polyline = step.getJSONObject("polyline");
                        String encodedPolyline = polyline.getString("encodedPolyline");

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
                valueAnimator.setDuration(1000);
                valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
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

// Lấy vị trí bắt đầu/kết thúc từ JSON của Route API
                JSONObject startLocation = legInfo.getJSONObject("startLocation").getJSONObject("latLng");
                JSONObject endLocation = legInfo.getJSONObject("endLocation").getJSONObject("latLng");

                String startLatLng = startLocation.getDouble("latitude") + "," + startLocation.getDouble("longitude");
                String endLatLng = endLocation.getDouble("latitude") + "," + endLocation.getDouble("longitude");

                addDestinationMarker();

                updateRouteInfo(origin, destination, durationText, distanceText);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
            }
        });
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
    private void getPlaceName(LatLng latLng, TextView textView) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                textView.setText(addresses.get(0).getAddressLine(0)); // Cập nhật TextView
            } else {
                textView.setText("Unknown location");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateRouteInfo(LatLng origin, LatLng destination, String durationText, String distanceText) {
        TextView tvOrigin = findViewById(R.id.tv_origin);
        TextView tvDestination = findViewById(R.id.tv_destination);
        TextView tvDurationDistance = findViewById(R.id.tv_duration_distance);

        //getPlaceName(origin, tvOrigin);
        getPlaceName(destination, tvDestination);
        tvDurationDistance.setText(durationText + " (" + distanceText + ")");
    }


}