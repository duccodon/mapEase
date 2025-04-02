package com.example.mapease;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mapease.AsyncTask.FetchPlacesTasks;
import com.example.mapease.Remote.RoutesAPIHelper;
import com.example.mapease.Utils.SlidingPanelHelper;
import com.example.mapease.adapter.ReviewAdapter;
import com.example.mapease.databinding.ActivityMainBinding;
import com.example.mapease.events.SendLocationToActivity;
import com.example.mapease.model.Review;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private PlacesClient placesClient;
    private Marker currentMarker = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private SearchView mapSearchView;
    private SupportMapFragment mapFragment;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private SlidingUpPanelLayout slidingLayout;
    private LinearLayout slidingPanel;
    private final String url = "https://api.openweathermap.org/data/2.5/weather?";
    private final String appId = "7b7b6b93a8b58cf10c77b14fc34e06fe";

    private  View searchView;

    private TextView placeName;
    private TextView placeAddress;
    private ImageView placeImage;
    private String currentLatitude = "";
    private String currentLongitude = "";
    private LatLng currentLatLng = null;
    private LatLng selectedLatLng = null;
    private String currentName = "";
    private String selectedName = "";
    TextView weather;
    DecimalFormat df = new DecimalFormat("#.##");
    //FusedLocationProviderClient locationProviderClient;
    ActivityMainBinding binding;

    //tab layout
    private TabLayout tabLayout;
    private LinearLayout overviewTab;
    private LinearLayout reviewsTab;
    private LinearLayout exploreTab;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        slidingLayout = findViewById(R.id.sliding_layout);
        slidingPanel = findViewById(R.id.sliding_panel);
        SlidingPanelHelper.setupPanel(this, slidingLayout, slidingPanel);
        //EdgeToEdge.enable(this);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        init();
        //locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(MainActivity.this);
        binding.mapTypeButton.setOnClickListener(v -> showMapTypeMenu(v));
        binding.profileButton.setOnClickListener(v -> showProfileMenu(v));
        weather = findViewById(R.id.weatherText);

        //tab layout
        tabLayout = findViewById(R.id.tab_layout);
        overviewTab = findViewById(R.id.overview_tab);
        reviewsTab = findViewById(R.id.reviews_tab);
        exploreTab = findViewById(R.id.explore_tab);
        //button for place types
        setupButtonListenersForPlacesType();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Overview tab
                        overviewTab.setVisibility(View.VISIBLE);
                        exploreTab.setVisibility(View.GONE);
                        reviewsTab.setVisibility(View.GONE);
                        break;
                    case 1: // Reviews tab
                        overviewTab.setVisibility(View.GONE);
                        exploreTab.setVisibility(View.GONE);
                        reviewsTab.setVisibility(View.VISIBLE);
                        break;
                    case 2: // Explore tab
                        overviewTab.setVisibility(View.GONE);
                        exploreTab.setVisibility(View.VISIBLE);
                        reviewsTab.setVisibility(View.GONE);
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.yourProfile) {
                startActivity(new Intent(getApplicationContext(), yourProfileActivity.class));
                return true;
            } else if (id == R.id.yourTimeLine) {
                return true;
            } else if (id == R.id.locationSharing) {
                return true;
            } else if (id == R.id.setting) {
                return true;
            }else if (id == R.id.logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, loginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        popup.show(); // Display the menu
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        myMap.setPadding(0, 220, 0, 220);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            return;
                        }
                        myMap.setMyLocationEnabled(true);
                        myMap.getUiSettings().setMyLocationButtonEnabled(true);
                        myMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e ->
                                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        if (location != null) {
                                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));

                                            // Update instance variables
                                            currentLatitude = String.valueOf(location.getLatitude());
                                            currentLongitude = String.valueOf(location.getLongitude());
                                            getWeatherDetails();
                                            currentLatLng = userLatLng;
                                        } else {
                                            Toast.makeText(MainActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            return true;
                        });
                        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 300);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission " + response.getPermissionName() + " was denied", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        //hide sliding panel before choose a place
        slidingPanel.setVisibility(View.GONE);

        // POI click listener
        myMap.setOnPoiClickListener(poi -> {
            slidingPanel.setVisibility(View.VISIBLE);

            Log.d("DetailInfor", "POI click" + poi.placeId);
            selectedLatLng = poi.latLng;
            currentLatitude = String.valueOf(poi.latLng.latitude);
            currentLongitude = String.valueOf(poi.latLng.longitude);
            selectedName = poi.name;

            getWeatherDetails();

            findPlaceDetailsFromLocation(poi.latLng, poi.name, poi.placeId);

            getReviews(true, poi.placeId);
        });

        // Normal map click listener
        myMap.setOnMapClickListener(latLng -> {
            slidingPanel.setVisibility(View.VISIBLE);

            Log.d("DetailInfor", "Normal click");
            selectedLatLng = latLng;
            currentLatitude = String.valueOf(latLng.latitude);
            currentLongitude = String.valueOf(latLng.longitude);

            getWeatherDetails();

            findPlaceDetailsFromLocation(latLng, null, null);
            getReviews(false, null);
        });
    }

    private void findPlaceDetailsFromLocation(LatLng latLng, String placeName, String placeID) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS
        );

        List<Place.Field> fullFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS,
                Place.Field.INTERNATIONAL_PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.OPENING_HOURS,
                Place.Field.PRICE_LEVEL,
                Place.Field.BUSINESS_STATUS
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            if (placeID != null) //poi click
            {
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(fields);
                placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
                    Log.d("DetailInfor", "Place ID: " + placeID);

                    FetchPlaceRequest fullRequest = FetchPlaceRequest.builder(placeID, fullFields).build();
                    Task<FetchPlaceResponse> fetchPlaceTask = placesClient.fetchPlace(fullRequest);
                    fetchPlaceTask.addOnSuccessListener(fullResponse -> {
                        Place place = fullResponse.getPlace();
                        Log.d("DetailInfor", "Full information: " + place);
                        updatePlaceUI(place, null, null);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("DetailInfor", "Error finding place: " + e.getMessage());
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DetailInfor", "Error finding place: " + e.getMessage());
                    getAddressFromLatLng(latLng);
                });
            }else{
                getAddressFromLatLng(latLng);
            }
        }else{
            Toast.makeText(MainActivity.this, "Error: Do not have access fine location permission", Toast.LENGTH_SHORT).show();
        }

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(fields);

            placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
                Place bestMatch = null;
                float minDistance = Float.MAX_VALUE;

                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Place place = placeLikelihood.getPlace();
                    float[] results = new float[1];
                    Location.distanceBetween(
                            place.getLatLng().latitude, place.getLatLng().longitude,
                            latLng.latitude, latLng.longitude,
                            results
                    );

                    if (results[0] < minDistance) {
                        minDistance = results[0];
                        bestMatch = place;
                    }
                }

                if (bestMatch != null && minDistance < 50) { // Within 50 meters
                    Log.d("DetailInfor", "Found Place ID: " + bestMatch.getId());

                    FetchPlaceRequest fullRequest = FetchPlaceRequest.builder(bestMatch.getId(), fullFields).build();
                    Task<FetchPlaceResponse> fetchPlaceTask = placesClient.fetchPlace(fullRequest);
                    fetchPlaceTask.addOnSuccessListener(fullResponse -> {
                        // Get the place object
                        Place place = fullResponse.getPlace();
                        // Do something with the place object
                        Log.d("DetailInfor", "Full information: " + fullResponse.getPlace());
                    });

                    updatePlaceUI(bestMatch);
                } else {
                    // Fallback to reverse geocoding
                    getAddressFromLatLng(latLng, placeName);
                }
            }).addOnFailureListener(e -> {
                Log.e("DetailInfor", "Error finding place: " + e.getMessage());
                getAddressFromLatLng(latLng, placeName);
            });
        } else {
            Log.d("DetailInfor", " Not permitted");
            getAddressFromLatLng(latLng, placeName);
        }*/
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Log.d("DetailInfor", "Normal Address");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                selectedName = address;
                Log.d("DetailInfor", "Address: " + address);
                updatePlaceUI(null, addresses.get(0), latLng);
            }
        } catch (IOException e) {
            Log.e("DetailInfor", "Error: " + e.getMessage());
            updatePlaceUI(null, null, latLng);
        }
    }

    /*private void getAddressFromLatLng(LatLng latLng, String placeName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            Place.Builder placeBuilder = Place.builder()
                    .setLatLng(latLng);

            if (placeName != null) {
                placeBuilder.setName(placeName);
            } else if (!addresses.isEmpty()) {
                placeBuilder.setName(addresses.get(0).getFeatureName())
                        .setAddress(addresses.get(0).getAddressLine(0));
            } else {
                placeBuilder.setName("Selected Location")
                        .setAddress(latLng.latitude + ", " + latLng.longitude);
            }

            updatePlaceUI(placeBuilder.build());
        } catch (IOException e) {
            Log.e("DetailInfor", "Geocoder error: " + e.getMessage());
            updatePlaceUI(Place.builder()
                    .setName(placeName != null ? placeName : "Selected Location")
                    .setAddress(latLng.latitude + ", " + latLng.longitude)
                    .setLatLng(latLng)
                    .build());
        }
    }*/

    private void updatePlaceUI(Place place, Address address, LatLng latLng) {
        runOnUiThread(() -> {
            TextView placeName = findViewById(R.id.location_title);
            TextView placeAddress = findViewById(R.id.place_address);
            ImageView placeImage = findViewById(R.id.place_image);
            TextView placePhone = findViewById(R.id.place_phone);
            TextView placeWebsite = findViewById(R.id.place_website);
            TextView placeRating = findViewById(R.id.place_rating);
            TextView duration_calc = findViewById(R.id.duration_calc);
            TextView distance_calc = findViewById(R.id.distance_calc);
            ImageView car_icon = findViewById(R.id.car_icon);


            if (place != null && address == null && latLng == null) {
                Log.d("DetailInfor", "Update UI" + place.toString());

                placeName.setText(place.getName() != null ? place.getName() : "Selected Location");
                placeAddress.setText(place.getAddress() != null ? place.getAddress() : "Address not available");

                // Handle optional fields
                placePhone.setVisibility(place.getPhoneNumber() != null ? View.VISIBLE : View.GONE);
                if (place.getPhoneNumber() != null) {
                    placePhone.setText("Phone: " + place.getPhoneNumber());
                }

                placeWebsite.setVisibility(place.getWebsiteUri() != null ? View.VISIBLE : View.GONE);
                if (place.getWebsiteUri() != null) {
                    placeWebsite.setText("Website: " + place.getWebsiteUri());
                }

                placeRating.setVisibility(place.getRating() != null ? View.VISIBLE : View.GONE);
                if (place.getRating() != null) {
                    placeRating.setText(String.format(Locale.getDefault(),
                            "Rating: %.1f (%d reviews)",
                            place.getRating(),
                            place.getUserRatingsTotal() != null ? place.getUserRatingsTotal() : 0));
                }

                // Handle photos
                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    loadPlacePhoto(place.getPhotoMetadatas().get(0), placeImage);
                } else {
                    placeImage.setVisibility(View.GONE);
                }

                // Update marker
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                currentMarker = myMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getName()));

                // Expand panel
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                //CALCULATE DISTANCE AND DURATION
                if (currentLatLng != null && place.getLatLng() != null) {
                    RoutesAPIHelper.requestRoute(this, currentLatLng.latitude , currentLatLng.longitude, place.getLatLng().latitude, place.getLatLng().longitude,
                            "routes.distanceMeters,routes.duration", "DRIVE", // ✅ Get distance and duration
                            response -> {
                                try {
                                    // Lấy data từ response JSON
                                    Log.d("API_RETURN", "Response JSON: " + response.toString());
                                    // Extract routes array
                                    JSONArray routesArray = response.getJSONArray("routes");
                                    JSONObject firstRoute = routesArray.getJSONObject(0);

                                    // Extract distance and duration
                                    int distance = firstRoute.getInt("distanceMeters"); // Distance in meters
                                    String durationString = firstRoute.getString("duration"); // Duration as "174s"

                                    // Convert duration string (e.g., "174s") to an integer value (in seconds)
                                    int duration = 0;
                                    if (durationString.endsWith("s")) {
                                        duration = Integer.parseInt(durationString.replace("s", "")); // Remove the "s" and convert to integer
                                    }

                                    // Log values for debugging
                                    Log.d("RouteInfo", "Distance: " + distance + " Duration: " + duration);

                                    // Kiểm tra nếu có dữ liệu thì hiển thị TextView
                                    if (distance > 0 && duration > 0) {
                                        distance_calc.setVisibility(View.VISIBLE);
                                        duration_calc.setVisibility(View.VISIBLE);
                                        car_icon.setVisibility(View.VISIBLE);
                                        if (distance < 1000) { // If distance is less than 1 km, show in meters
                                            distance_calc.setText(distance + " m");
                                        } else { // If distance is more than or equal to 1 km, show in kilometers
                                            double distanceInKm = distance / 1000.0;
                                            distance_calc.setText(String.format("%.2f km", distanceInKm));
                                        }

                                        if (duration < 60) { // If duration is less than 1 minute, show in seconds
                                            duration_calc.setText(duration + " sec");
                                        } else { // If duration is more than or equal to 1 minute, show in minutes
                                            int minutes = duration / 60;
                                            duration_calc.setText(minutes + " min");
                                        }

                                    } else {
                                        Log.d("RouteError", "Invalid distance or duration");
                                    }
                                } catch (JSONException e) {
                                    Log.d("RouteError", "Error parsing response: " + e.getMessage());
                                }
                            });
                }
            }
            else {

                String fullAddress = address.getAddressLine(0);

                // Update UI with address info
                placeName.setText("Selected Location");
                placeAddress.setText(fullAddress);

                // Hide optional fields
                placePhone.setVisibility(View.GONE);
                placeWebsite.setVisibility(View.GONE);
                placeRating.setVisibility(View.GONE);

                distance_calc.setVisibility(View.GONE);
                duration_calc.setVisibility(View.GONE);
                car_icon.setVisibility(View.GONE);

                // Set default photo
                placeImage.setVisibility(View.VISIBLE);
                placeImage.setImageResource(R.drawable.default_location);

                // Update marker
                if (currentMarker != null) {
                    currentMarker.remove();
                }


                currentMarker = myMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Selected Location"));

                // Expand panel
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                Log.d("DetailInfor", "Address: " + fullAddress);
            }
        });
    }

    //Load reviews
    private void getReviews(boolean isPOI, String locationID){
        LinearLayout reviewsTab = findViewById(R.id.reviews_tab);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        AppCompatButton writeReviewButton = findViewById(R.id.write_review_button);

        if(isPOI) {
            //enable tab
            reviewsTab.setVisibility(View.VISIBLE);
            writeReviewButton.setVisibility(View.VISIBLE);
            tabLayout.getTabAt(1).view.setEnabled(true);
            tabLayout.getTabAt(1).view.setAlpha(1f);

            tabLayout.getTabAt(2).view.setEnabled(true);
            tabLayout.getTabAt(2).view.setAlpha(1f);

            tabLayout.selectTab(tabLayout.getTabAt(0));
            // Sample user data for reviews ZbfpPC8DkegTta8kYEjdORcw6cs2
            //location id testing ChIJzfOsShkvdTERSeoX_lSUTOk ChIJzfOsShkvdTERSeoX_lSUTOk
            List<String> images1 = Arrays.asList(
                    "https://imgur.com/DvpvklR.png",
                    "https://picsum.photos/200/300"
            );

            Map<String, Boolean> likesForReview2 = new HashMap<>();
            likesForReview2.put("ZbfpPC8DkegTta8kYEjdORcw6cs2", true);

            List<Review> reviews = new ArrayList<>();
            reviews.add(new Review(
                    "1",
                    "ZbfpPC8DkegTta8kYEjdORcw6cs2",
                    "LocationID",
                    "Greate service, nice staff",
                    5.0f,
                    "yyyy-MM-dd'T'HH:mm:ssZ",
                    images1,
                    new HashMap<>()
            ));
            reviews.add(new Review(
                    "2",
                    "ZbfpPC8DkegTta8kYEjdORcw6cs2",
                    "LocationID",
                    "Haidilao vạn hạnh mall , nhan vien nhiet tinh",
                    4.0f,
                    "yyyy-MM-dd'T'HH:mm:ssZ",
                    images1,
                    likesForReview2
            ));

            // Set up ListView
            ListView listView = findViewById(R.id.reviewListView);
            ReviewAdapter adapter = new ReviewAdapter(this, reviews);
            listView.setAdapter(adapter);

            // review button
            writeReviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WriteReview.class);
                    intent.putExtra("context", "main");
                    Log.e("WriteReview", "Location ID " + locationID);
                    intent.putExtra("locationID", locationID);
                    startActivity(intent);
                }
            });
        }else{
            reviewsTab.setVisibility(View.GONE);
            writeReviewButton.setVisibility(View.GONE);

            // Disable the Reviews tab in TabLayout
            tabLayout.getTabAt(1).view.setEnabled(false);
            tabLayout.getTabAt(1).view.setAlpha(0.5f);

            //test explore tab
            tabLayout.getTabAt(2).view.setEnabled(false);
            tabLayout.getTabAt(2).view.setAlpha(0.5f);

            // Ensure we're showing the Overview tab
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }
    }

    public void loadPlacePhoto(PhotoMetadata photoMetadata, ImageView imageView) {
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build();

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }).addOnFailureListener(exception -> {
            Log.e("PlacePhoto", "Error loading photo: " + exception.getMessage());
            imageView.setVisibility(View.GONE);
        });
    }

    private void showMapTypeMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_map_type, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.mapNone) {
                myMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            } else if (id == R.id.mapNormal) {
                myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            } else if (id == R.id.mapSatellite) {
                myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            } else if (id == R.id.mapHybrid) {
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            } else if (id == R.id.mapTerrain) {
                myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            }
            return false;
        });
        popup.show(); // Display the menu
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.mapNone){
            myMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }

        if(id == R.id.mapNormal){
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        if(id == R.id.mapSatellite){
            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        if(id == R.id.mapHybrid){
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        if(id == R.id.mapTerrain){
            myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

        return super.onOptionsItemSelected(item);
    }
    private void init()
    {
        Places.initialize(this, getString(R.string.ggMapAPIKey));
        placesClient = Places.createClient(this);


        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);
        assert autocompleteSupportFragment != null;

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.FORMATTED_ADDRESS, Place.Field.DISPLAY_NAME, Place.Field.LAT_LNG));
        autocompleteSupportFragment.setHint(getString(R.string.search_here));

        if (autocompleteSupportFragment != null) {
            searchView = autocompleteSupportFragment.getView();
            if (searchView != null) {
                searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_search_view));
            }
        }

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLocation() != null) {
                    currentLatitude = String.valueOf(place.getLatLng().latitude);
                    currentLongitude = String.valueOf(place.getLatLng().longitude);
                    currentName = place.getDisplayName();
                    selectedName = place.getDisplayName();
                    getWeatherDetails();

                    selectedLatLng = place.getLatLng();

                    placeName = findViewById(R.id.location_title);
                    placeAddress = findViewById(R.id.place_address);
                    placeImage = findViewById(R.id.place_image);

                    // Update instance variables
                    placeName.setText(place.getName());
                    placeAddress.setText("Address: " + place.getAddress());

                    // get place details
                    List<Place.Field> fullFields = Arrays.asList(
                            Place.Field.ID,
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.LAT_LNG,
                            Place.Field.PHOTO_METADATAS,
                            Place.Field.INTERNATIONAL_PHONE_NUMBER,
                            Place.Field.WEBSITE_URI,
                            Place.Field.OPENING_HOURS,
                            Place.Field.PRICE_LEVEL,
                            Place.Field.BUSINESS_STATUS,
                            Place.Field.TYPES
                    );
                    FetchPlaceRequest fullRequest = FetchPlaceRequest.builder(place.getId(), fullFields).build();
                    placesClient.fetchPlace(fullRequest).addOnSuccessListener(fullResponse -> {
                        Place fullPlace = fullResponse.getPlace();
                        // Check if this is a POI by examining types
                        boolean isPOI = isPointOfInterest(fullPlace.getTypes());

                        if (isPOI) {
                            //POI search
                            getWeatherDetails();
                            findPlaceDetailsFromLocation(fullPlace.getLatLng(), fullPlace.getName(), fullPlace.getId());

                            getReviews(true, fullPlace.getId());
                        } else {
                            getWeatherDetails();
                            findPlaceDetailsFromLocation(place.getLatLng(), null, null);

                            getReviews(false, null);
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this,"Failed to fetch place details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                    // Expand the sliding panel
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                    updateMapLocation(place.getLatLng(), place.getName());
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "No coordinates found for this place!",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(@NonNull Status status) {
            }
        });
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .setMinUpdateDistanceMeters(1f)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                currentLatLng = newPosition;

                // Sử dụng animateCamera thay vì moveCamera
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, myMap.getCameraPosition().zoom),
                        1000, // Thời gian animation: 1 giây
                        null); // Không cần callback
                setRestrictPlacesInCountry(locationResult.getLastLocation());
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private boolean isPointOfInterest(List<Place.Type> types) {
        if (types == null) return false;

        // Common POI types - you can expand this list
        for (Place.Type type : types) {
            switch (type) {
                case ACCOUNTING:
                case AIRPORT:
                case AMUSEMENT_PARK:
                case AQUARIUM:
                case ART_GALLERY:
                case ATM:
                case BAKERY:
                case BANK:
                case BAR:
                case BEAUTY_SALON:
                case BICYCLE_STORE:
                case BOOK_STORE:
                case BOWLING_ALLEY:
                case BUS_STATION:
                case CAFE:
                case CAMPGROUND:
                case CAR_DEALER:
                case CAR_RENTAL:
                case CAR_REPAIR:
                case CAR_WASH:
                case CASINO:
                case CEMETERY:
                case CHURCH:
                case CITY_HALL:
                case CLOTHING_STORE:
                case CONVENIENCE_STORE:
                case COURTHOUSE:
                case DENTIST:
                case DEPARTMENT_STORE:
                case DOCTOR:
                case DRUGSTORE:
                case ELECTRICIAN:
                case ELECTRONICS_STORE:
                case EMBASSY:
                case FIRE_STATION:
                case FLORIST:
                case FUNERAL_HOME:
                case FURNITURE_STORE:
                case GAS_STATION:
                case GYM:
                case HAIR_CARE:
                case HARDWARE_STORE:
                case HINDU_TEMPLE:
                case HOME_GOODS_STORE:
                case HOSPITAL:
                case INSURANCE_AGENCY:
                case JEWELRY_STORE:
                case LAUNDRY:
                case LAWYER:
                case LIBRARY:
                case LIGHT_RAIL_STATION:
                case LIQUOR_STORE:
                case LOCAL_GOVERNMENT_OFFICE:
                case LOCKSMITH:
                case LODGING:
                case MEAL_DELIVERY:
                case MEAL_TAKEAWAY:
                case MOSQUE:
                case MOVIE_RENTAL:
                case MOVIE_THEATER:
                case MOVING_COMPANY:
                case MUSEUM:
                case NIGHT_CLUB:
                case PAINTER:
                case PARK:
                case PARKING:
                case PET_STORE:
                case PHARMACY:
                case PHYSIOTHERAPIST:
                case PLUMBER:
                case POLICE:
                case POST_OFFICE:
                case PRIMARY_SCHOOL:
                case REAL_ESTATE_AGENCY:
                case RESTAURANT:
                case ROOFING_CONTRACTOR:
                case RV_PARK:
                case SCHOOL:
                case SECONDARY_SCHOOL:
                case SHOE_STORE:
                case SHOPPING_MALL:
                case SPA:
                case STADIUM:
                case STORAGE:
                case STORE:
                case SUBWAY_STATION:
                case SUPERMARKET:
                case SYNAGOGUE:
                case TAXI_STAND:
                case TOURIST_ATTRACTION:
                case TRAIN_STATION:
                case TRANSIT_STATION:
                case TRAVEL_AGENCY:
                case UNIVERSITY:
                case VETERINARY_CARE:
                case ZOO:
                    return true;
                default:
                    // Not a POI type
            }
        }
        return false;
    }

    private void getPlacePhoto(String placeId, ImageView placeImage) {
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();

            if (photoMetadataList == null || photoMetadataList.isEmpty()) {
                Log.e("PlacePhoto", "No photo metadata found for this place.");
                placeImage.setVisibility(View.GONE);
                return;
            }

            PhotoMetadata photoMetadata = photoMetadataList.get(0);

            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500)
                    .setMaxHeight(300)
                    .build();

            placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                placeImage.setImageBitmap(bitmap);
                placeImage.setVisibility(View.VISIBLE);
            }).addOnFailureListener(exception -> {
                Log.e("PlacePhoto", "Photo fetch failed: " + exception.getMessage());
                placeImage.setVisibility(View.GONE);
            });

        }).addOnFailureListener(exception -> {
            Log.e("PlacePhoto", "Place details fetch failed: " + exception.getMessage());
            placeImage.setVisibility(View.GONE);

        });

    }

    private void setRestrictPlacesInCountry(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if(addressList.size() > 0)
                autocompleteSupportFragment.setCountries(addressList.get(0).getCountryCode());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateMapLocation(LatLng latLng, String placeName) {

        // Remove the previous marker if it exists
        if (currentMarker != null) {
            currentMarker.remove();
        }
        // Add a new marker
        currentMarker = myMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(placeName));
        // Move the camera to the new location with zoom level
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)); // 15f is zoom level
    }

    public void getWeatherDetails() {
        String tempUrl = "";
        String lat = currentLatitude;
        String lon = currentLongitude;
        ImageView weatherIcon = findViewById(R.id.weather_icon);
        TextView weatherCity = findViewById(R.id.weather_city);

        if (lat.isEmpty() || lon.isEmpty()) {
            weather.setTextColor(Color.RED);
            weather.setText("Please select a specific location!");
            weatherIcon.setImageResource(R.drawable.ic_error); // Error icon
            weatherCity.setText("No Location");
            return;
        }

        tempUrl = url + "lat=" + lat + "&lon=" + lon + "&appid=" + appId;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                String description = jsonObjectWeather.getString("description");
                String weatherCode = jsonObjectWeather.getString("icon"); // Weather icon code
                JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                double temp = jsonObjectMain.getDouble("temp") - 273.15;
                double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                int humidity = jsonObjectMain.getInt("humidity");
                float pressure = jsonObjectMain.getInt("pressure");
                JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                String wind = jsonObjectWind.getString("speed");
                JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                String clouds = jsonObjectClouds.getString("all");
                JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                String countryName = jsonObjectSys.getString("country");
                String cityName = jsonResponse.getString("name");

                // Update city name
                weatherCity.setText(cityName + " (" + countryName + ")");

                // Set weather icon based on weatherCode
                switch (weatherCode) {
                    case "01d": case "01n":
                        weatherIcon.setImageResource(R.drawable.ic_sunny);
                        break;
                    case "02d": case "02n":
                        weatherIcon.setImageResource(R.drawable.ic_partly_cloudy);
                        break;
                    case "03d": case "03n": case "04d": case "04n":
                        weatherIcon.setImageResource(R.drawable.ic_cloudy);
                        break;
                    case "09d": case "09n": case "10d": case "10n":
                        weatherIcon.setImageResource(R.drawable.ic_rain);
                        break;
                    case "11d": case "11n":
                        weatherIcon.setImageResource(R.drawable.ic_thunderstorm);
                        break;
                    case "13d": case "13n":
                        weatherIcon.setImageResource(R.drawable.ic_snow);
                        break;
                    default:
                        weatherIcon.setImageResource(R.drawable.ic_weather_default);
                }

                // Format weather output
                String output = "Temp: " + df.format(temp) + " °C\n" +
                        "Feels Like: " + df.format(feelsLike) + " °C\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Description: " + description + "\n" +
                        "Wind Speed: " + wind + " m/s\n" +
                        "Cloudiness: " + clouds + "%\n" +
                        "Pressure: " + pressure + " hPa";
                weather.setTextColor(Color.parseColor("#34495E"));
                weather.setText(output);

            } catch (JSONException e) {
                e.printStackTrace();
                weather.setText("Error parsing weather data!");
                weatherIcon.setImageResource(R.drawable.ic_cloud);
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            weather.setText("Failed to fetch weather data!");
            weatherIcon.setImageResource(R.drawable.ic_cloud);
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void setupSlidingPanel() {
        slidingLayout = findViewById(R.id.sliding_layout);
        slidingPanel = findViewById(R.id.sliding_panel);
        if (slidingLayout == null || slidingPanel == null) {
            Log.e("SlidingPanel", "SlidingUpPanelLayout, panel, or drag handle not found!");
            return;
        }

        // Enable touch gestures for dragging
        slidingLayout.setTouchEnabled(true);

        // Set initial state to COLLAPSED
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        // Calculate 80% of screen height for EXPANDED state
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int maxPanelHeight = (int) (screenHeight * 0.9); // 80% of screen height

        // Set the maximum height for the sliding panel
        ViewGroup.LayoutParams params = slidingPanel.getLayoutParams();
        params.height = maxPanelHeight; // Cap EXPANDED at 80%
        slidingPanel.setLayoutParams(params);
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView == null) {
            Log.e("SlidingPanel", "Root view not found!");
            return;
        }
        // Monitor panel movement (optional, for debugging)
        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.d("SlidingPanel", "Sliding: " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.d("SlidingPanel", "State Changed: " + previousState + " -> " + newState);

                // Handle the three states
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    rootView.setEnabled(true); // Enable everything behind the panel
                    Log.d("SlidingPanel", "COLLAPSED: Background interactive");
                } else if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    rootView.setEnabled(true); // Keep background interactive
                    Log.d("SlidingPanel", "ANCHORED: Background interactive");
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    rootView.setEnabled(false); // Disable everything behind the panel
                    Log.d("SlidingPanel", "EXPANDED: Background untouchable");
                }
            }
        });

        // Click on drag_handle: ANCHORED <-> EXPANDED
        slidingPanel.setOnClickListener(view -> {
            SlidingUpPanelLayout.PanelState currentState = slidingLayout.getPanelState();
            Log.d("SlidingPanel", "Clicked! Current State: " + currentState);

            if (currentState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED); // COLLAPSED -> ANCHORED (40%)

            } else if (currentState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED); // ANCHORED -> EXPANDED (80%)

            } else if (currentState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED); // EXPANDED -> COLLAPSED (100dp)
            }
        });
    }

    public void routingFunction(View view) {
        // Kiểm tra null trước để tránh crash
        if (currentLatLng == null || selectedLatLng == null) {
            Toast.makeText(this, "Missing location data: " +
                            (currentLatLng == null ? "Current is null" : "") + " " +
                            (selectedLatLng == null ? "Selected is null" : ""),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Parse LatLng thành String và hiển thị qua Toast
        String currentStr = "Current: " + currentLatLng.latitude + ", " + currentLatLng.longitude;
        String selectedStr = "Selected: " + selectedLatLng.latitude + ", " + selectedLatLng.longitude;
        Toast.makeText(this, currentStr + "\n" + selectedStr, Toast.LENGTH_LONG).show();

        // Gửi dữ liệu và chuyển Activity
        Intent intent = new Intent(this, RouteActivity.class);
        EventBus.getDefault().postSticky(new SendLocationToActivity(currentLatLng, selectedLatLng, currentName, selectedName));
        startActivity(intent);
    }

    public void setupButtonListenersForPlacesType() {
        findViewById(R.id.btn_restaurant).setOnClickListener(v -> fetchNearbyPlaces("restaurant"));
        findViewById(R.id.btn_hotel).setOnClickListener(v -> fetchNearbyPlaces("hotel"));
        findViewById(R.id.btn_cafe).setOnClickListener(v -> fetchNearbyPlaces("cafe"));
        findViewById(R.id.btn_mall).setOnClickListener(v -> fetchNearbyPlaces("mall"));

    }

    public void fetchNearbyPlaces(String placeType) {
        if (currentLatLng == null) {
            Toast.makeText(this, "Fetching location, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location=" + currentLatLng.latitude + "," + currentLatLng.longitude
                + "&radius=1500"
                + "&type=" + placeType
                + "&key=" + getString(R.string.ggMapAPIKey);

        new FetchPlacesTasks(myMap).execute(url);
    }
}
