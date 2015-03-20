package com.mridang.sunmoon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.luckycatlabs.moonphase.MoonPhaseCalculator;

import org.acra.ACRA;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/*
 * This class is the main class that provides the widget
 */
public class SunmoonWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.sunmoon.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.sunmoon.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.sunmoon.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return null;
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d(getTag(), "Calculating the moon-phase and sunrise/sunset times");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);

		try {

			Location locLocation = null;
			LocationManager mgrLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			List<String> lstProviders = mgrLocation.getProviders(true);
			if (lstProviders != null) {

				for (String strProvider : lstProviders) {

					if (mgrLocation.getLastKnownLocation(strProvider) != null) {

						Double dblLatitude = mgrLocation.getLastKnownLocation(strProvider).getLatitude();
						Double dblLongitude = mgrLocation.getLastKnownLocation(strProvider).getLongitude();
						locLocation = new Location(dblLatitude, dblLongitude);

					}

				}

				if (locLocation == null) {
					Log.w(getTag(), "Unable to get any sort of location data");
					return;
				}

			}

			DateFormat sdfTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
			String strTimezone = TimeZone.getDefault().getDisplayName();
			SunriseSunsetCalculator sunCalculator = new SunriseSunsetCalculator(locLocation, strTimezone);
			MoonPhaseCalculator mooPhase = new MoonPhaseCalculator();

			Calendar calToday = Calendar.getInstance();
			Calendar calSunrise = sunCalculator.getCivilSunriseCalendarForDate(calToday);
			Calendar calSunset = sunCalculator.getCivilSunsetCalendarForDate(calToday);
			String strSunset = sdfTime.format(calSunset.getTime());
			String strSunrise = sdfTime.format(calSunrise.getTime());
			Integer intMoonphase = mooPhase.getMoonPhaseForDate(calToday);
			String strMoonphase = getResources().getStringArray(R.array.phases)[intMoonphase];

			if (Calendar.getInstance().after(calSunrise)) {

				if (Calendar.getInstance().after(calSunset)) {

					calToday.add(Calendar.DATE, 1);
					calSunrise = sunCalculator.getCivilSunriseCalendarForDate(calToday);
					calSunset = sunCalculator.getCivilSunsetCalendarForDate(calToday);
					strSunset = sdfTime.format(calSunset.getTime());
					strSunrise = sdfTime.format(calSunrise.getTime());
					edtInformation.expandedTitle(getString(R.string.moonphase, strMoonphase));
					edtInformation.expandedBody(getString(R.string.sunrise, strSunrise, strSunset));

				} else {

					calToday.add(Calendar.DATE, 1);
					strSunrise = sdfTime.format(sunCalculator.getCivilSunriseCalendarForDate(calToday).getTime());
					edtInformation.expandedBody(getString(R.string.moonphase, strMoonphase));
					edtInformation.expandedTitle(getString(R.string.sunset, strSunset, strSunrise));

				}

			} else {

				edtInformation.expandedBody(getString(R.string.moonphase, strMoonphase));
				edtInformation.expandedTitle(getString(R.string.sunrise, strSunrise, strSunset));

			}

			edtInformation.visible(true);

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.sunmoon.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
		onUpdateData(UPDATE_REASON_MANUAL);
	}

}