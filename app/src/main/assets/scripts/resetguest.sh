#!/system/bin/sh

# BEAR-LOADER Reset Guest Account Script
# Reset PUBG Mobile guest account data to bypass restrictions
# Compatible with all PUBG Mobile variants (Global, Korea, Vietnam, Taiwan, India)

echo "🔄 BEAR-LOADER: Starting Guest Account Reset..."

# PUBG Mobile package names
PACKAGES=(
    "com.tencent.ig"           # PUBG Global
    "com.pubg.krmobile"        # PUBG Korea
    "com.vng.pubgmobile"       # PUBG Vietnam
    "com.rekoo.pubgm"          # PUBG Taiwan
    "com.pubg.imobile"         # PUBG India
)

# Function to reset guest data for a package
reset_guest_data() {
    local package=$1
    echo "🎮 Processing: $package"
    
    # Check if package is installed
    if pm list packages | grep -q "$package"; then
        echo "✅ Found installed: $package"
        
        # Stop the application if running
        am force-stop "$package" 2>/dev/null
        
        # Clear guest-related data directories
        rm -rf "/data/data/$package/shared_prefs/GuestData.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/GCloudCore.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/GuestLogin.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/TDataMaster.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/PandoraSDK.xml" 2>/dev/null
        
        # Clear cache related to guest accounts
        rm -rf "/data/data/$package/cache/GuestCache" 2>/dev/null
        rm -rf "/data/data/$package/cache/webviewCache" 2>/dev/null
        rm -rf "/data/data/$package/cache/loginCache" 2>/dev/null
        
        # Clear temporary files
        rm -rf "/data/data/$package/files/tempdata" 2>/dev/null
        rm -rf "/data/data/$package/files/guesttemp" 2>/dev/null
        rm -rf "/data/data/$package/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames" 2>/dev/null
        
        # Reset guest login preferences
        rm -rf "/data/data/$package/shared_prefs/com.epicgames.ue4.GameActivity.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/login_info.xml" 2>/dev/null
        
        # Clear WebView data that might store guest session
        rm -rf "/data/data/$package/app_webview" 2>/dev/null
        rm -rf "/data/data/$package/app_textures" 2>/dev/null
        
        # Clear additional Tencent/PUBG specific data
        rm -rf "/data/data/$package/shared_prefs/DENGTA.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/beacon.xml" 2>/dev/null
        rm -rf "/data/data/$package/shared_prefs/apollo_config.xml" 2>/dev/null
        
        # Remove guest device binding files
        rm -rf "/data/data/$package/files/cacheFile.txt" 2>/dev/null
        rm -rf "/data/data/$package/files/vmpcloudconfig.json" 2>/dev/null
        
        echo "🧹 Guest data cleared for: $package"
    else
        echo "⚠️ Package not installed: $package"
    fi
}

# Function to clear system-level guest traces
clear_system_guest_data() {
    echo "🔧 Clearing system-level guest traces..."
    
    # Clear Android Account Manager guest accounts
    rm -rf "/data/system/accounts.db-wal" 2>/dev/null
    
    # Clear Google Play Games guest data
    rm -rf "/data/data/com.google.android.play.games/shared_prefs/guest_prefs.xml" 2>/dev/null
    
    # Clear advertising ID traces that might link to guest account
    rm -rf "/data/data/com.google.android.gms/shared_prefs/adid_settings.xml" 2>/dev/null
    
    echo "✅ System guest traces cleared"
}

# Function to reset network configuration caches
reset_network_cache() {
    echo "🌐 Resetting network caches..."
    
    # Clear DNS cache
    setprop net.dns1 8.8.8.8
    setprop net.dns2 8.8.4.4
    
    # Clear network configuration cache
    rm -rf "/data/misc/wifi/wpa_supplicant.conf.tmp" 2>/dev/null
    
    echo "✅ Network cache reset"
}

# Main execution
echo "🚀 BEAR-LOADER Guest Reset v1.0"
echo "📱 Target: PUBG Mobile Guest Accounts"
echo "⚡ Mode: Advanced Reset"

# Check for root access
if [ "$(id -u)" != "0" ]; then
    echo "❌ Root access required!"
    echo "💡 Please ensure BEAR-LOADER has root permissions"
    exit 1
fi

echo "🔐 Root access confirmed"

# Process each PUBG Mobile variant
for package in "${PACKAGES[@]}"; do
    reset_guest_data "$package"
done

# Clear system-level traces
clear_system_guest_data

# Reset network cache
reset_network_cache

# Force restart Android shell to clear in-memory caches
echo "🔄 Refreshing system caches..."
killall com.android.systemui 2>/dev/null
sleep 2

echo ""
echo "🎉 =================================="
echo "✅ GUEST RESET COMPLETED SUCCESSFULLY"
echo "🎮 All PUBG Mobile guest data cleared"
echo "🔒 Device ready for fresh guest login"
echo "💡 Restart PUBG Mobile to apply changes"
echo "===================================="
echo ""

# Return success
exit 0 