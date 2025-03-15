package com.example.mapease;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mapease.databinding.ActivityMainBinding;
import com.example.mapease.events.SendLocationToActivity;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private Marker currentMarker = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private SearchView mapSearchView;
    private SupportMapFragment mapFragment;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private SlidingUpPanelLayout slidingLayout;
    private final String url = "https://api.openweathermap.org/data/2.5/weather?";
    private final String appId = "7b7b6b93a8b58cf10c77b14fc34e06fe";

    private String currentLatitude = "";
    private String currentLongitude = "";
    private LatLng currentLatLng = null;
    private LatLng selectedLatLng = null;
    private boolean btnWeatherCheck = false;
    ImageButton btnWeather;
    TextView weather, weatherNoti;
    DecimalFormat df = new DecimalFormat("#.##");
    //FusedLocationProviderClient locationProviderClient;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupSlidingPanel();
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
        btnWeather = findViewById(R.id.weather_button);
        weather = findViewById(R.id.weatherText);
        weatherNoti = findViewById(R.id.weatherNoti);
    }

    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.yourProfile) {
                myMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            } else if (id == R.id.yourTimeLine) {
                myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            } else if (id == R.id.locationSharing) {
                myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            } else if (id == R.id.setting) {
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            }
            return false;
        });
        popup.show(); // Display the menu
    }


    /*private void getLastLocation() {
        TasK<Location> task = fusedLocationProviderClient.getLastLocation();
    }*/
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
                                                binding.latitude.setText("Latitude: "+ String.valueOf(location.getLatitude()));
                                                binding.longitude.setText("Longtitude: "+String.valueOf(location.getLongitude()));
                                                // Update instance variables
                                                currentLatitude = String.valueOf(location.getLatitude());
                                                currentLongitude = String.valueOf(location.getLongitude());
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
        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);
        assert autocompleteSupportFragment != null;
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.FORMATTED_ADDRESS, Place.Field.DISPLAY_NAME, Place.Field.LAT_LNG));
        autocompleteSupportFragment.setHint(getString(R.string.search_here));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLocation() != null) {
                    binding.latitude.setText("Latitude: "+ String.valueOf(place.getLatLng().latitude));
                    binding.longitude.setText("Longtitude: "+String.valueOf(place.getLatLng().longitude));
                    currentLatitude = String.valueOf(place.getLatLng().latitude);
                    currentLongitude = String.valueOf(place.getLatLng().longitude);
                    selectedLatLng = place.getLatLng();

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
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .setMinUpdateDistanceMeters(10f)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                currentLatLng = newPosition;
                myMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
                setRestrictPlacesInCountry(locationResult.getLastLocation());
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
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

    public void getWeatherDetails(View view) {
        if (!btnWeatherCheck) {
            weather.setVisibility(View.VISIBLE);
            btnWeatherCheck = true;
            weatherNoti.setVisibility(View.VISIBLE);
            weatherNoti.setText("Slide up for more details");
            weatherNoti.setTextColor(Color.parseColor("#E74C3C")); // Red color for noti
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                weatherNoti.setVisibility(View.GONE);
            }, 3000);
        }

        String tempUrl = "";
        String lat = currentLatitude;
        String lon = currentLongitude;
        ImageView weatherIcon = findViewById(R.id.weather_icon);
        TextView weatherCity = findViewById(R.id.weather_city);

        if (lat.isEmpty() || lon.isEmpty()) {
            weather.setTextColor(Color.RED);
            weather.setText("Please select a specific location!");
            btnWeatherCheck = false;
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
                weatherIcon.setImageResource(R.drawable.map_type);
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            weather.setText("Failed to fetch weather data!");
            weatherIcon.setImageResource(R.drawable.map_type);
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    private void setupSlidingPanel() {
        slidingLayout = findViewById(R.id.sliding_layout);
        LinearLayout slidingPanel = findViewById(R.id.sliding_panel);
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
        EventBus.getDefault().postSticky(new SendLocationToActivity(currentLatLng, selectedLatLng));
        startActivity(intent);
    }
}