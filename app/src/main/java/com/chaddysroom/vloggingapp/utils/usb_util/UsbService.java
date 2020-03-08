package com.chaddysroom.vloggingapp.utils.usb_util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;

import static android.content.Context.USB_SERVICE;

public class UsbService
{
    private static final String TAG = "UsbService";
    private Context mContext;

    // Usb Members
    private UsbDevice mUsbDevice = null;
    private UsbSerialDevice mUsbSerialDevice = null;
    private UsbDeviceConnection mUsbConnection = null;
    private int mBaudRate = 9600;

    public static final String ACTION_USB_PERMISSION = "com.mainactivity.USB_PERMISSION";
    private static final int ARDUINO_USB_VID = 0x2341; // Java should be able to accept hex for int literals
    private static final int ARDUINO_USB_PID = 0x0001;

    // This acts as a config variable. When connecting to a USB device, only accept devices that
    // have ARDUINO_USB_VID and ARDUINO_USB_PID for vendor and product ids respectively.
    private static Boolean B_CHECK_DEVICE_ID = false;

    private final UsbSerialInterface.UsbReadCallback mUsbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data)
        {

        }
    };

    /*  Note on USB Accessories
     *   You will notice that when dealing with permissions and the USB protocol in general, we
     *   deal with actions such as the attachment of USB Accessories versus USB Devices. This
     *   appears to be a semantic description of the mode of operation. When in USB Device mode
     *   the android powered device (our phone) acts as the host and powers the bus. Conversely,
     *   when in accessory mode, the attached peripheral/device acts as the host and powers the bus.
     *
     *   Generally, we are accustomed to the fact that attached USB peripherals act as host, so we
     *   will use ACTION_USB_ACCESSORY_xxxxxx actions.
     *
     *   See: https://developer.android.com/guide/topics/connectivity/usb/index.html
     */
    private final BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
//            Toast.makeText(context, "Received USB broadcast", Toast.LENGTH_SHORT).show();

            if(intent.getAction().equals(ACTION_USB_PERMISSION))
            {
                Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

                // We can attempt to setup connection now
                if(granted)
                {
                    Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT).show();

                    //UsbManager manager = (UsbManager) context.getSystemService(UsbManager.class);
                    UsbManager manager = (UsbManager) context.getSystemService(USB_SERVICE);
                    assert manager != null;

                    mUsbConnection = manager.openDevice(mUsbDevice);
                    mUsbSerialDevice = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, mUsbConnection);

                    // Acquired valid serial device. Can now set serial properties such as Baud Rate
                    // For UART on the Arduino, the default configuration is as follows:
                    /*
                     *   8 Data Bits
                     *   1 Stop Bit
                     *   1 Start Bit (Implied)
                     *   No Parity Bits
                     */
                    // As far as I am concerned, the USB-Serial device on the Arduino only uses
                    // TX and RX wire. Hence I assume there cannot exist any flow control.
                    if(mUsbSerialDevice != null && mUsbSerialDevice.open())
                    {
                        mUsbSerialDevice.setBaudRate(mBaudRate);
                        mUsbSerialDevice.setDataBits(UsbSerialDevice.DATA_BITS_8);
                        mUsbSerialDevice.setParity(UsbSerialDevice.PARITY_NONE);
                        mUsbSerialDevice.setStopBits(UsbSerialDevice.STOP_BITS_1);
                        mUsbSerialDevice.setFlowControl(UsbSerialDevice.FLOW_CONTROL_OFF);
                        mUsbSerialDevice.read(mUsbReadCallback);

                        Toast.makeText(context, "Serial device successfully configured!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG,"Selected Baudrate: " + mBaudRate);
                    }
                    else
                    {
                        Log.w(TAG, "Failed to open serial device");
                        Toast.makeText(context, "Failed to open Serial device", Toast.LENGTH_SHORT).show();
                        disconnectUsb();
                    }
                }
                else
                {
                    Log.w(TAG, "Usb permission not granted");
                    Toast.makeText(context, "USB permission not granted", Toast.LENGTH_SHORT).show();
                    disconnectUsb();
                }
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED))
            {
                startUsbConnecting();
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED))
            {
                disconnectUsb();
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
            {
                Toast.makeText(context, "USB Device Attached", Toast.LENGTH_SHORT).show();
                startUsbConnecting();
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
            {
                Toast.makeText(context, "USB Device Detached", Toast.LENGTH_SHORT).show();
                disconnectUsb();
            }
        }
    };

    public UsbService(Context context)
    {
        mContext = context;
        Log.i(TAG, "USB SERVICE INITIALIZED");
    }

    public BroadcastReceiver GetUsbBroadcastReceiver()
    {
        return mUsbBroadcastReceiver;
    }

    /****************************************************************
                                USB Methods
     ****************************************************************/
    public boolean startUsbConnecting()
    {
        // Already connected
        if(mUsbSerialDevice != null)
            return false;

        UsbManager manager = (UsbManager) mContext.getSystemService(USB_SERVICE);
        assert manager != null;

        HashMap<String, UsbDevice> devices = manager.getDeviceList();

        for(UsbDevice device : devices.values())
        {
            mUsbDevice = device;

            if(B_CHECK_DEVICE_ID
                    && (device.getVendorId() != ARDUINO_USB_VID
                    || device.getProductId() != ARDUINO_USB_PID))
            {
                continue;
            }

            if(UsbSerialDevice.isSupported(device))
            {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                manager.requestPermission(device, pendingIntent);
                return true;
            }
        }

        // Reset if nothing was found
        mUsbDevice = null;
        mUsbConnection = null;

        return false;
    }

    public boolean sendData(byte[] data)
    {
        if(mUsbSerialDevice == null || mUsbConnection == null)
        {
            Log.i(TAG, "No data was sent. No connection to a USB/Serial device found");
            //Toast.makeText(this, "No Serial Connection", Toast.LENGTH_SHORT).show();
            return false;
        }
        mUsbSerialDevice.write(data);

        Log.i(TAG, "Write Serial Data: " + data);
        return true;
    }

    public boolean disconnectUsb()
    {
        mUsbSerialDevice.close();
        mUsbSerialDevice = null;

        mUsbConnection.close();
        mUsbConnection = null;

        mUsbDevice = null;

        return true;
    }
}
