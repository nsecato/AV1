// Generated by view binder compiler. Do not edit!
package com.example.av1.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.av1.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMapsBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final TextView averageSpeed;

  @NonNull
  public final ImageButton buttonGenerateRoute;

  @NonNull
  public final ImageButton buttonStartSimulation;

  @NonNull
  public final ImageButton buttonStopSimulation;

  @NonNull
  public final TextView currentDistance;

  @NonNull
  public final TextView currentSpeed;

  @NonNull
  public final TextView currentTime;

  @NonNull
  public final TextView eficiency;

  @NonNull
  public final AutoCompleteTextView endLocation;

  @NonNull
  public final TextView fuelConsumption;

  @NonNull
  public final FragmentContainerView map;

  @NonNull
  public final TextView optmalSpeed;

  @NonNull
  public final TextView totalDistance;

  @NonNull
  public final TextView totalTime;

  private ActivityMapsBinding(@NonNull CoordinatorLayout rootView, @NonNull TextView averageSpeed,
      @NonNull ImageButton buttonGenerateRoute, @NonNull ImageButton buttonStartSimulation,
      @NonNull ImageButton buttonStopSimulation, @NonNull TextView currentDistance,
      @NonNull TextView currentSpeed, @NonNull TextView currentTime, @NonNull TextView eficiency,
      @NonNull AutoCompleteTextView endLocation, @NonNull TextView fuelConsumption,
      @NonNull FragmentContainerView map, @NonNull TextView optmalSpeed,
      @NonNull TextView totalDistance, @NonNull TextView totalTime) {
    this.rootView = rootView;
    this.averageSpeed = averageSpeed;
    this.buttonGenerateRoute = buttonGenerateRoute;
    this.buttonStartSimulation = buttonStartSimulation;
    this.buttonStopSimulation = buttonStopSimulation;
    this.currentDistance = currentDistance;
    this.currentSpeed = currentSpeed;
    this.currentTime = currentTime;
    this.eficiency = eficiency;
    this.endLocation = endLocation;
    this.fuelConsumption = fuelConsumption;
    this.map = map;
    this.optmalSpeed = optmalSpeed;
    this.totalDistance = totalDistance;
    this.totalTime = totalTime;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMapsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMapsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_maps, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMapsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.average_speed;
      TextView averageSpeed = ViewBindings.findChildViewById(rootView, id);
      if (averageSpeed == null) {
        break missingId;
      }

      id = R.id.button_generate_route;
      ImageButton buttonGenerateRoute = ViewBindings.findChildViewById(rootView, id);
      if (buttonGenerateRoute == null) {
        break missingId;
      }

      id = R.id.button_start_simulation;
      ImageButton buttonStartSimulation = ViewBindings.findChildViewById(rootView, id);
      if (buttonStartSimulation == null) {
        break missingId;
      }

      id = R.id.button_stop_simulation;
      ImageButton buttonStopSimulation = ViewBindings.findChildViewById(rootView, id);
      if (buttonStopSimulation == null) {
        break missingId;
      }

      id = R.id.current_distance;
      TextView currentDistance = ViewBindings.findChildViewById(rootView, id);
      if (currentDistance == null) {
        break missingId;
      }

      id = R.id.current_speed;
      TextView currentSpeed = ViewBindings.findChildViewById(rootView, id);
      if (currentSpeed == null) {
        break missingId;
      }

      id = R.id.current_time;
      TextView currentTime = ViewBindings.findChildViewById(rootView, id);
      if (currentTime == null) {
        break missingId;
      }

      id = R.id.eficiency;
      TextView eficiency = ViewBindings.findChildViewById(rootView, id);
      if (eficiency == null) {
        break missingId;
      }

      id = R.id.end_location;
      AutoCompleteTextView endLocation = ViewBindings.findChildViewById(rootView, id);
      if (endLocation == null) {
        break missingId;
      }

      id = R.id.fuel_consumption;
      TextView fuelConsumption = ViewBindings.findChildViewById(rootView, id);
      if (fuelConsumption == null) {
        break missingId;
      }

      id = R.id.map;
      FragmentContainerView map = ViewBindings.findChildViewById(rootView, id);
      if (map == null) {
        break missingId;
      }

      id = R.id.optmal_speed;
      TextView optmalSpeed = ViewBindings.findChildViewById(rootView, id);
      if (optmalSpeed == null) {
        break missingId;
      }

      id = R.id.total_distance;
      TextView totalDistance = ViewBindings.findChildViewById(rootView, id);
      if (totalDistance == null) {
        break missingId;
      }

      id = R.id.total_time;
      TextView totalTime = ViewBindings.findChildViewById(rootView, id);
      if (totalTime == null) {
        break missingId;
      }

      return new ActivityMapsBinding((CoordinatorLayout) rootView, averageSpeed,
          buttonGenerateRoute, buttonStartSimulation, buttonStopSimulation, currentDistance,
          currentSpeed, currentTime, eficiency, endLocation, fuelConsumption, map, optmalSpeed,
          totalDistance, totalTime);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
