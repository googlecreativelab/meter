package com.androidexperiments.meter.drawers;

import android.app.Service;
import android.content.Context;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

import com.androidexperiments.meter.R;

/**
 * Renders Cellular connection data, inherits from TriangleFillDrawer
 */
public class CellularDrawer extends TriangleFillDrawer {
    private final String TAG = this.getClass().getSimpleName();


    private boolean firstRead = true;

    private static boolean isAirplaneModeOn(Context context)
    {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0 ) != 0;
    }

    TelephonyManager tManager;

    public CellularDrawer(final Context context){
        super(
                context,
                context.getResources().getColor(R.color.cellular_background),
                context.getResources().getColor(R.color.cellular_triangle_background),
                context.getResources().getColor(R.color.cellular_triangle_foreground),
                context.getResources().getColor(R.color.cellular_triangle_critical)
        );

        this.label1 = "Cellular";

        tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        setLabel2();

        tManager.listen(new PhoneStateListener(){
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                Log.d(TAG, "SIGNAL "+String.valueOf(signalStrength));

                int level = 0;
                String tech = "";

                if( isAirplaneModeOn(context) ){
                    percent = 0f;
                    connected = false;
                    label1 = "No connection";
                    label2 = "Airplane Mode Enabled";
                    return;
                }

                List<CellInfo> infos = tManager.getAllCellInfo();
                if( infos == null ){
                    connected = false;
                    return;
                }

                for (final CellInfo info : infos) {
                    if (info instanceof CellInfoWcdma) {
                        final CellSignalStrengthWcdma  wcdma = ((CellInfoWcdma) info).getCellSignalStrength();

                        if(level < wcdma.getLevel()) {
                            level = wcdma.getLevel();
                            tech = "wcdma";
                        }
                    } else if (info instanceof CellInfoGsm) {
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();

                        if(level < gsm.getLevel()) {
                            level = gsm.getLevel();
                            tech = "gsm";
                        }
                    } else if (info instanceof CellInfoCdma) {
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();

                        if(level < cdma.getLevel()) {
                            level = cdma.getLevel();
                            tech = "cdma";
                        }
                    } else if (info instanceof CellInfoLte) {
                        final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();

                        if(level < lte.getLevel()) {
                            level = lte.getLevel();
                            tech = "lte";
                        }

                    }
                }

                connected = true;
                label1 = "Cellular";
                percent = (float) (level / 4.0);

                if (firstRead) {
                    firstRead = false;
                    _percent = (float) (percent-0.001);
                }

                Log.d(TAG, tech+" "+String.valueOf(level));
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
                setLabel2();
                Log.d(TAG,"STATE "+String.valueOf(serviceState)+"   "+serviceState.getState());
            }
        },PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);

    }

    private void setLabel2() {
        String type = getNetworkType();
        label2 = tManager.getNetworkOperatorName();
        if(!type.equals("Unknown")) {
            label2 += " " + type;
        }
    }


    protected String getNetworkType(){
        switch (tManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

}
