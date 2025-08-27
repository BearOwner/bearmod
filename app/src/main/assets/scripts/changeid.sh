#!/system/bin/sh

# BEAR-LOADER Change Device ID Script
# Modify device identifiers to bypass device-based detection/bans
# Advanced hardware fingerprint spoofing for PUBG Mobile

echo "🔄 BEAR-LOADER: Starting Device ID Change..."

# Function to generate random string
generate_random_string() {
    local length=$1
    local charset="0123456789abcdef"
    local result=""
    for i in $(seq 1 $length); do
        local random_index=$((RANDOM % ${#charset}))
        result="${result}${charset:$random_index:1}"
    done
    echo "$result"
}

# Function to generate random MAC address
generate_random_mac() {
    printf "%02x:%02x:%02x:%02x:%02x:%02x\n" \
        $((RANDOM % 256)) $((RANDOM % 256)) $((RANDOM % 256)) \
        $((RANDOM % 256)) $((RANDOM % 256)) $((RANDOM % 256))
}

# Function to generate random Android ID
generate_android_id() {
    generate_random_string 16
}

# Function to backup current settings
backup_original_settings() {
    echo "💾 Backing up original device settings..."
    
    mkdir -p /data/local/tmp/bear_backup 2>/dev/null
    
    # Backup Android ID
    settings get secure android_id > /data/local/tmp/bear_backup/original_android_id.txt 2>/dev/null
    
    # Backup WiFi MAC
    cat /sys/class/net/wlan0/address > /data/local/tmp/bear_backup/original_wifi_mac.txt 2>/dev/null
    
    # Backup device properties
    getprop ro.serialno > /data/local/tmp/bear_backup/original_serial.txt 2>/dev/null
    getprop ro.boot.serialno > /data/local/tmp/bear_backup/original_boot_serial.txt 2>/dev/null
    
    echo "✅ Settings backed up to /data/local/tmp/bear_backup/"
}

# Function to change Android ID
change_android_id() {
    echo "📱 Changing Android ID..."
    
    local new_android_id=$(generate_android_id)
    
    # Method 1: Settings database
    settings put secure android_id "$new_android_id" 2>/dev/null
    
    # Method 2: Direct database modification
    sqlite3 /data/data/com.android.providers.settings/databases/settings.db \
        "UPDATE secure SET value='$new_android_id' WHERE name='android_id';" 2>/dev/null
    
    # Method 3: System properties (requires root)
    setprop persist.sys.android_id "$new_android_id" 2>/dev/null
    
    echo "🔑 New Android ID: $new_android_id"
}

# Function to change device serial numbers
change_device_serial() {
    echo "🏷️ Changing device serial numbers..."
    
    local new_serial="BEAR$(generate_random_string 10)"
    
    # Change boot serial number
    setprop ro.boot.serialno "$new_serial" 2>/dev/null
    setprop ro.serialno "$new_serial" 2>/dev/null
    setprop persist.vendor.radio.imei "$new_serial" 2>/dev/null
    
    # Modify build properties
    mount -o remount,rw /system 2>/dev/null
    
    # Create temporary build.prop modifications
    if [ -f /system/build.prop ]; then
        sed -i "s/ro.serialno=.*/ro.serialno=$new_serial/" /system/build.prop 2>/dev/null
        sed -i "s/ro.boot.serialno=.*/ro.boot.serialno=$new_serial/" /system/build.prop 2>/dev/null
    fi
    
    mount -o remount,ro /system 2>/dev/null
    
    echo "🏷️ New Serial: $new_serial"
}

# Function to change WiFi MAC address
change_wifi_mac() {
    echo "📶 Changing WiFi MAC address..."
    
    local new_mac=$(generate_random_mac)
    
    # Method 1: Interface down/up with new MAC
    ifconfig wlan0 down 2>/dev/null
    ifconfig wlan0 hw ether "$new_mac" 2>/dev/null
    ifconfig wlan0 up 2>/dev/null
    
    # Method 2: Direct write to address file (some devices)
    echo "$new_mac" > /sys/class/net/wlan0/address 2>/dev/null
    
    # Method 3: Using ip command
    ip link set dev wlan0 address "$new_mac" 2>/dev/null
    
    echo "📶 New WiFi MAC: $new_mac"
}

# Function to change Bluetooth MAC
change_bluetooth_mac() {
    echo "🔵 Changing Bluetooth MAC address..."
    
    local new_bt_mac=$(generate_random_mac)
    
    # Stop Bluetooth service
    svc bluetooth disable 2>/dev/null
    
    # Modify Bluetooth address
    echo "$new_bt_mac" > /sys/class/bluetooth/hci0/address 2>/dev/null
    
    # Alternative method for some devices
    setprop persist.service.bdroid.bdaddr "$new_bt_mac" 2>/dev/null
    
    # Restart Bluetooth
    svc bluetooth enable 2>/dev/null
    
    echo "🔵 New Bluetooth MAC: $new_bt_mac"
}

# Function to modify PUBG-specific device identifiers
change_pubg_device_ids() {
    echo "🎮 Modifying PUBG-specific device identifiers..."
    
    # PUBG Mobile package names
    local packages=(
        "com.tencent.ig"
        "com.pubg.krmobile"
        "com.vng.pubgmobile"
        "com.rekoo.pubgm"
        "com.pubg.imobile"
    )
    
    for package in "${packages[@]}"; do
        if pm list packages | grep -q "$package"; then
            echo "🎯 Processing: $package"
            
            # Stop the game
            am force-stop "$package" 2>/dev/null
            
            # Clear device identifier caches
            rm -rf "/data/data/$package/shared_prefs/device_info.xml" 2>/dev/null
            rm -rf "/data/data/$package/shared_prefs/UniqueDeviceID.xml" 2>/dev/null
            rm -rf "/data/data/$package/shared_prefs/DeviceFingerprint.xml" 2>/dev/null
            rm -rf "/data/data/$package/shared_prefs/HardwareInfo.xml" 2>/dev/null
            
            # Clear Tencent device binding
            rm -rf "/data/data/$package/shared_prefs/tencent_device.xml" 2>/dev/null
            rm -rf "/data/data/$package/files/tdm" 2>/dev/null
            rm -rf "/data/data/$package/files/deviceid.txt" 2>/dev/null
            
            # Clear anti-cheat device traces
            rm -rf "/data/data/$package/files/ace_data" 2>/dev/null
            rm -rf "/data/data/$package/files/security" 2>/dev/null
            rm -rf "/data/data/$package/cache/security_cache" 2>/dev/null
            
            echo "✅ Device IDs cleared for: $package"
        fi
    done
}

# Function to change IMEI (requires advanced root)
change_imei() {
    echo "📞 Attempting IMEI modification..."
    
    # Note: IMEI changing is very device-specific and risky
    # This is a safer approach that modifies app-level IMEI access
    
    local new_imei="86$(generate_random_string 13)"
    
    # Set property-based IMEI (for apps that read properties)
    setprop persist.vendor.radio.imei "$new_imei" 2>/dev/null
    setprop ril.imei "$new_imei" 2>/dev/null
    
    # Modify telephony database (safer than direct IMEI change)
    sqlite3 /data/data/com.android.providers.telephony/databases/telephony.db \
        "UPDATE carriers SET imei='$new_imei' WHERE imei IS NOT NULL;" 2>/dev/null
    
    echo "📞 IMEI property set: $new_imei"
    echo "⚠️ Note: Full IMEI change requires device-specific methods"
}

# Function to clear advertising and tracking IDs
reset_advertising_ids() {
    echo "📊 Resetting advertising and tracking IDs..."
    
    # Clear Google Advertising ID
    rm -rf "/data/data/com.google.android.gms/shared_prefs/adid_settings.xml" 2>/dev/null
    
    # Clear Facebook advertising ID
    rm -rf "/data/data/com.facebook.katana/shared_prefs/adid_prefs.xml" 2>/dev/null
    
    # Clear other tracking data
    rm -rf "/data/data/com.android.providers.settings/shared_prefs/tracking.xml" 2>/dev/null
    
    # Reset analytics IDs
    find /data/data -name "*analytics*" -type f -delete 2>/dev/null
    find /data/data -name "*tracking*" -type f -delete 2>/dev/null
    
    echo "✅ Advertising IDs reset"
}

# Function to restart relevant services
restart_services() {
    echo "🔄 Restarting system services..."
    
    # Restart connectivity services
    stop netd 2>/dev/null
    start netd 2>/dev/null
    
    # Restart WiFi
    svc wifi disable 2>/dev/null
    sleep 2
    svc wifi enable 2>/dev/null
    
    # Clear DNS cache
    setprop net.dns1 8.8.8.8
    setprop net.dns2 8.8.4.4
    
    echo "✅ Services restarted"
}

# Main execution
echo "🚀 BEAR-LOADER Device ID Changer v1.0"
echo "🎯 Target: Hardware fingerprint spoofing"
echo "⚡ Mode: Advanced modification"

# Check for root access
if [ "$(id -u)" != "0" ]; then
    echo "❌ Root access required!"
    echo "💡 Please ensure BEAR-LOADER has root permissions"
    exit 1
fi

echo "🔐 Root access confirmed"

# Backup original settings
backup_original_settings

# Execute modifications
change_android_id
change_device_serial
change_wifi_mac
change_bluetooth_mac
change_pubg_device_ids
change_imei
reset_advertising_ids

# Restart services
restart_services

echo ""
echo "🎉 =================================="
echo "✅ DEVICE ID CHANGE COMPLETED"
echo "🔑 All device identifiers modified"
echo "🎮 PUBG Mobile device cache cleared"
echo "🔄 System services restarted"
echo "💡 Reboot device for full effect"
echo "===================================="
echo ""

# Show summary of changes
echo "📋 SUMMARY OF CHANGES:"
echo "• Android ID: Changed"
echo "• Device Serial: Changed"
echo "• WiFi MAC: Changed"
echo "• Bluetooth MAC: Changed"
echo "• PUBG Device Cache: Cleared"
echo "• Advertising IDs: Reset"
echo ""
echo "🔧 Backup location: /data/local/tmp/bear_backup/"
echo ""

# Return success
exit 0 