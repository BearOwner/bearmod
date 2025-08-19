#ifndef JOIN_SKIN_H
#define JOIN_SKIN_H

#include "Helper/Includes.h"
#include "Helper/struct.h"
#include <sys/types.h>

// Forward declarations
extern std::map<std::string, u_long> Config;

// ===========================================================================================================
// SKIN STRUCTURE DEFINITIONS
// ===========================================================================================================

struct snew_Skin {
    // Character Skins
    [[maybe_unused]] int XSuits = 403003;
    [[maybe_unused]] int XSuits1 = 40604002;
    [[maybe_unused]] int Balo1 = 501001;
    [[maybe_unused]] int Balo2 = 501002;
    [[maybe_unused]] int Balo3 = 501003;
    [[maybe_unused]] int Balo4 = 501004;
    [[maybe_unused]] int Balo5 = 501005;
    [[maybe_unused]] int Balo6 = 501006;
    [[maybe_unused]] int Helmet1 = 502001;
    [[maybe_unused]] int Helmet2 = 502002;
    [[maybe_unused]] int Helmet3 = 502003;
    [[maybe_unused]] int Helmet4 = 502004;
    [[maybe_unused]] int Helmet5 = 502005;
    [[maybe_unused]] int Helmet6 = 502114;
    [[maybe_unused]] int Helmet7 = 502115;
    [[maybe_unused]] int Helmet8 = 502116;
    int Parachute = 703001;

    // Assault Rifles
    int AKM = 101001;
    int AKM_Mag = 291001;
    int M16A4 = 101002;
    int M16A4_Stock = 205007;
    int M16A4_Mag = 291002;
    int Scar = 101003;
    int Scar_Mag = 291003;
    int M416_1 = 101004;
    int M416_2 = 291004;
    int M416_3 = 203008;
    int M416_4 = 205005;
    int M416_flash = 201010;
    int M416_compe = 201009;
    int M416_silent = 201011;
    int M416_reddot = 203001;
    int M416_holo = 203001;
    int M416_x2 = 203003;
    int M416_x3 = 203014;
    int M416_x4 = 203004;
    int M416_x6 = 203015;
    int M416_quickMag = 204012;
    int M416_extendedMag = 204011;
    int M416_quickNextended = 204013;
    int M416_stock = 205002;
    int M416_verical = 203015;
    int M416_angle = 202001;
    int M416_lightgrip = 202004;
    int M416_pink = 202005;
    int M416_lazer = 202007;
    int M416_thumb = 202006;
    int Groza = 101005;
    int QBZ = 101007;
    int AUG = 101006;
    int M762 = 101008;
    int M762_Mag = 291008;
    int ACE32 = 101102;
    int Honey = 101012;

    // SMGs
    int UZI = 102001;
    int UMP = 102002;
    int Vector = 102003;
    int Thompson = 102004;
    int Bizon = 102005;

    // Sniper Rifles
    int K98 = 103001;
    int M24 = 103002;
    int AWM = 103003;
    int AMR = 103012;
    int VSS = 103005;
    int SKS = 103004;
    int Mini14 = 103006;
    int MK14 = 103007;
    int SLR = 103009;

    // Shotguns
    int S1897 = 104002;

    // LMGs
    int DP28 = 105002;
    int M249 = 105001;
    int MG3 = 105010;
    int M249s = 205009;

    // Other Weapons
    int Skorpion = 106008;
    int Pan = 108004;

    // Vehicles
    int Moto = 1901001;
    int CoupeRP = 1961001;
    int Dacia = 1903001;
    int UAZ = 1908001;
    int Bigfoot = 1953001;
    int Mirado = 1914004;
    int OMirado = 1915001;
    int Buggy = 1907001;
    int MiniBus = 1904001;
    int Boat = 1911001;

    // Equipment Levels
    int baglv1 = 501001;
    int baglv2 = 501002;
    int baglv3 = 501003;
    int helmetlv1 = 502001;
    int helmetlv2 = 502002;
    int helmetlv3 = 502003;
};

// Global skin instance
inline snew_Skin new_Skin;

// ===========================================================================================================
// SKIN UPDATE FUNCTION
// ===========================================================================================================

void updateSkin() {
    // AKM Skins
    if (Config["SKIN_AKM"] == 0) {
        new_Skin.AKM = 101001; // Default AKM
        new_Skin.AKM_Mag = 205005;
    }
    if (Config["SKIN_AKM"] == 1) {
        new_Skin.AKM = 1101001089; // Glacier - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010010891;
    }
    if (Config["SKIN_AKM"] == 2) {
        new_Skin.AKM = 1101001103; // Desert Fossil - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010011031;
    }
    if (Config["SKIN_AKM"] == 3) {
        new_Skin.AKM = 1101001116; // Jack-o'-lantern - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010011161;
    }
    if (Config["SKIN_AKM"] == 4) {
        new_Skin.AKM = 1101001128; // Ghillie Dragon - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010011281;
    }
    if (Config["SKIN_AKM"] == 5) {
        new_Skin.AKM = 1101001143; // Gold Pirate - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010011431;
    }
    if (Config["SKIN_AKM"] == 6) {
        new_Skin.AKM = 1101001154; // Codebreaker - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010011541;
    }
    if (Config["SKIN_AKM"] == 7) {
        new_Skin.AKM = 1101001174; // Wandering Tyrant - AKM (Lv. 8)
        new_Skin.AKM_Mag = 1010011741;
    }
    if (Config["SKIN_AKM"] == 8) {
        new_Skin.AKM = 1101001213; // Starsea Admiral - AKM (Lv. 8)
        new_Skin.AKM_Mag = 1010012131;
    }
    if (Config["SKIN_AKM"] == 9) {
        new_Skin.AKM = 1101001231; // Bunny Munchkin - AKM (Lv. 7)
        new_Skin.AKM_Mag = 1010012311;
    }
    if (Config["SKIN_AKM"] == 10) {
        new_Skin.AKM = 1101001242; // Decisive Day - AKM (Lv. 8)
        new_Skin.AKM_Mag = 1010012421;
    }

    // Kar98K Skins
    if (Config["SKIN_KAR98K"] == 0)
        new_Skin.K98 = 103001; // Default
    if (Config["SKIN_KAR98K"] == 1)
        new_Skin.K98 = 1103001060; // Terror Fang - Kar98K (Lv. 7)
    if (Config["SKIN_KAR98K"] == 2)
        new_Skin.K98 = 1103001079; // Kukulkan Fury - Kar98K (Lv. 7)
    if (Config["SKIN_KAR98K"] == 3)
        new_Skin.K98 = 1103001101; // Moonlit Grace - Kar98K (Lv. 7)
    if (Config["SKIN_KAR98K"] == 4)
        new_Skin.K98 = 1103001146; // Titanium Shark - Kar98K (Lv. 7)
    if (Config["SKIN_KAR98K"] == 5)
        new_Skin.K98 = 1103001160; // Nebula Hunter - Kar98K (Lv. 5)
    if (Config["SKIN_KAR98K"] == 6)
        new_Skin.K98 = 1103001179; // Violet Volt - Kar98K (Lv. 7)

    // AWM Skins
    if (Config["SKIN_AWM"] == 0)
        new_Skin.AWM = 103003; // Default
    if (Config["SKIN_AWM"] == 1)
        new_Skin.AWM = 1103003022; // Mauve Avenger - AWM (Lv. 7)
    if (Config["SKIN_AWM"] == 2)
        new_Skin.AWM = 1103003030; // Field Commander - AWM (Lv. 7)
    if (Config["SKIN_AWM"] == 3)
        new_Skin.AWM = 1103003042; // Godzilla - AWM (Lv. 7)
    if (Config["SKIN_AWM"] == 4)
        new_Skin.AWM = 1103003051; // Rainbow Drake - AWM (Lv. 7)
    if (Config["SKIN_AWM"] == 5)
        new_Skin.AWM = 1103003062; // Flamewave - AWM (Lv. 7)
    if (Config["SKIN_AWM"] == 6)
        new_Skin.AWM = 1103003087; // Serpengleam - AWM (Lv. 7)
    if (Config["SKIN_AWM"] == 7)
        new_Skin.AWM = 1103003055; // Bramble Overlord - AWM

    // M24 Skins
    if (Config["SKIN_M24"] == 0)
        new_Skin.M24 = 103002; // Default
    if (Config["SKIN_M24"] == 1)
        new_Skin.M24 = 1103002018;
    if (Config["SKIN_M24"] == 2)
        new_Skin.M24 = 1103002030;
    if (Config["SKIN_M24"] == 3)
        new_Skin.M24 = 1103002049;
    if (Config["SKIN_M24"] == 4)
        new_Skin.M24 = 1103002059;
    if (Config["SKIN_M24"] == 5)
        new_Skin.M24 = 1103002087;

    // ACE32 Skins
    if (Config["SKIN_ACE32"] == 0)
        new_Skin.ACE32 = 101102; // Default
    if (Config["SKIN_ACE32"] == 1)
        new_Skin.ACE32 = 1101102007;
    if (Config["SKIN_ACE32"] == 2)
        new_Skin.ACE32 = 1101102017;

    // Vector Skins
    if (Config["SKIN_VECTOR"] == 0)
        new_Skin.Vector = 102003; // Default
    if (Config["SKIN_VECTOR"] == 1)
        new_Skin.Vector = 1102003020;
    if (Config["SKIN_VECTOR"] == 2)
        new_Skin.Vector = 1102003031;
    if (Config["SKIN_VECTOR"] == 3)
        new_Skin.Vector = 1102003065;
    if (Config["SKIN_VECTOR"] == 4)
        new_Skin.Vector = 1102003080;

    // UMP45 Skins
    if (Config["SKIN_UMP45"] == 0)
        new_Skin.UMP = 102002; // Default
    if (Config["SKIN_UMP45"] == 1)
        new_Skin.UMP = 1102002043;
    if (Config["SKIN_UMP45"] == 2)
        new_Skin.UMP = 1102002061;
    if (Config["SKIN_UMP45"] == 3)
        new_Skin.UMP = 1102002090;
    if (Config["SKIN_UMP45"] == 4)
        new_Skin.UMP = 1102002117;
    if (Config["SKIN_UMP45"] == 5)
        new_Skin.UMP = 1102002124;
    if (Config["SKIN_UMP45"] == 6)
        new_Skin.UMP = 1102002129;
    if (Config["SKIN_UMP45"] == 7)
        new_Skin.UMP = 1102002136;

    // UZI Skins
    if (Config["SKIN_UZI"] == 0)
        new_Skin.UZI = 102001; // Default
    if (Config["SKIN_UZI"] == 1)
        new_Skin.UZI = 1102001024;
    if (Config["SKIN_UZI"] == 2)
        new_Skin.UZI = 1102001036;
    if (Config["SKIN_UZI"] == 3)
        new_Skin.UZI = 1102001058;
    if (Config["SKIN_UZI"] == 4)
        new_Skin.UZI = 1102001069;
    if (Config["SKIN_UZI"] == 5)
        new_Skin.UZI = 1102001089;
    if (Config["SKIN_UZI"] == 6)
        new_Skin.UZI = 1102001102;

    // Thompson Skins
    if (Config["SKIN_THOMPSON"] == 0)
        new_Skin.Thompson = 102004; // Default
    if (Config["SKIN_THOMPSON"] == 1)
        new_Skin.Thompson = 1102004018; // Candy Cane - Thompson SMG (Lv. 5)
    if (Config["SKIN_THOMPSON"] == 2)
        new_Skin.Thompson = 1102004034; // Steampunk - Thompson SMG (Lv. 5)

    // M16A4 Skins
    if (Config["SKIN_M16A4"] == 0) {
        new_Skin.M16A4 = 101002; // Default
        new_Skin.M16A4_Stock = 205007;
        new_Skin.M16A4_Mag = 291002;
    }
    if (Config["SKIN_M16A4"] == 1) {
        new_Skin.M16A4 = 1101002029;
        new_Skin.M16A4_Stock = 1010020292;
        new_Skin.M16A4_Mag = 1010020291;
    }
    if (Config["SKIN_M16A4"] == 2) {
        new_Skin.M16A4 = 1101002056;
        new_Skin.M16A4_Stock = 1010020562;
        new_Skin.M16A4_Mag = 1010020561;
    }
    if (Config["SKIN_M16A4"] == 3) {
        new_Skin.M16A4 = 1101002068;
        new_Skin.M16A4_Stock = 1010020682;
        new_Skin.M16A4_Mag = 1010020681;
    }
    if (Config["SKIN_M16A4"] == 4) {
        new_Skin.M16A4 = 1101002081;
        new_Skin.M16A4_Stock = 1010020812;
        new_Skin.M16A4_Mag = 1010020811;
    }
    if (Config["SKIN_M16A4"] == 5) {
        new_Skin.M16A4 = 1101002103;
        new_Skin.M16A4_Stock = 1010021032;
        new_Skin.M16A4_Mag = 1010021031;
    }

    // AUG Skins
    if (Config["SKIN_AUG"] == 0)
        new_Skin.AUG = 101006; // Default
    if (Config["SKIN_AUG"] == 1)
        new_Skin.AUG = 1101006033;
    if (Config["SKIN_AUG"] == 2)
        new_Skin.AUG = 1101006044;
    if (Config["SKIN_AUG"] == 3)
        new_Skin.AUG = 1101006062;

    // Groza Skins
    if (Config["SKIN_GROZAR"] == 0)
        new_Skin.Groza = 101005; // Default
    if (Config["SKIN_GROZAR"] == 1)
        new_Skin.Groza = 1101005019;
    if (Config["SKIN_GROZAR"] == 2)
        new_Skin.Groza = 1101005025;
    if (Config["SKIN_GROZAR"] == 3)
        new_Skin.Groza = 1101005038;
    if (Config["SKIN_GROZAR"] == 4)
        new_Skin.Groza = 1101005043;
    if (Config["SKIN_GROZAR"] == 5)
        new_Skin.Groza = 1101005052;
    if (Config["SKIN_GROZAR"] == 6)
        new_Skin.Groza = 1101005082;

    // DP28 Skins
    if (Config["SKIN_DP28"] == 0)
        new_Skin.DP28 = 105002; // Default
    if (Config["SKIN_DP28"] == 1)
        new_Skin.DP28 = 1105002018;
    if (Config["SKIN_DP28"] == 2)
        new_Skin.DP28 = 1105002035;
    if (Config["SKIN_DP28"] == 3)
        new_Skin.DP28 = 1105002058;
    if (Config["SKIN_DP28"] == 4)
        new_Skin.DP28 = 1105002063;

    // M249 Skins
    if (Config["SKIN_M249"] == 0) {
        new_Skin.M249 = 105001; // Default
        new_Skin.M249s = 205009;
    }
    if (Config["SKIN_M249"] == 1) {
        new_Skin.M249 = 1105001020;
        new_Skin.M249s = 1050010351;
    }
    if (Config["SKIN_M249"] == 2) {
        new_Skin.M249 = 1105001034;
        new_Skin.M249s = 1050010412;
    }
    if (Config["SKIN_M249"] == 3) {
        new_Skin.M249 = 1105001048;
        new_Skin.M249s = 1050010482;
    }
    if (Config["SKIN_M249"] == 4) {
        new_Skin.M249 = 1105001054;
        new_Skin.M249s = 1050010542;
    }

    // SCAR-L Skins
    if (Config["SKIN_SCARL"] == 0) {
        new_Skin.Scar = 101003; // Default
        new_Skin.Scar_Mag = 291003;
    }
    if (Config["SKIN_SCARL"] == 1) {
        new_Skin.Scar = 1101003057;
        new_Skin.Scar_Mag = 1010030571;
    }
    if (Config["SKIN_SCARL"] == 2) {
        new_Skin.Scar = 1101003072;
        new_Skin.Scar_Mag = 1010030721;
    }
    if (Config["SKIN_SCARL"] == 3) {
        new_Skin.Scar = 1101003085;
        new_Skin.Scar_Mag = 1010030851;
    }
    if (Config["SKIN_SCARL"] == 4) {
        new_Skin.Scar = 1101003098;
        new_Skin.Scar_Mag = 1010030981;
    }
    if (Config["SKIN_SCARL"] == 5) {
        new_Skin.Scar = 1101003112;
        new_Skin.Scar_Mag = 1010031121;
    }
    if (Config["SKIN_SCARL"] == 6) {
        new_Skin.Scar = 1101003125;
        new_Skin.Scar_Mag = 1010031251;
    }
    if (Config["SKIN_SCARL"] == 7) {
        new_Skin.Scar = 1101003138;
        new_Skin.Scar_Mag = 1010031381;
    }

    // M762 Skins
    if (Config["SKIN_M762"] == 0) {
        new_Skin.M762 = 101008; // Default
        new_Skin.M762_Mag = 291008;
    }
    if (Config["SKIN_M762"] == 1) {
        new_Skin.M762 = 1101008015;
        new_Skin.M762_Mag = 1010080151;
    }
    if (Config["SKIN_M762"] == 2) {
        new_Skin.M762 = 1101008028;
        new_Skin.M762_Mag = 1010080281;
    }
    if (Config["SKIN_M762"] == 3) {
        new_Skin.M762 = 1101008041;
        new_Skin.M762_Mag = 1010080411;
    }
    if (Config["SKIN_M762"] == 4) {
        new_Skin.M762 = 1101008054;
        new_Skin.M762_Mag = 1010080541;
    }
    if (Config["SKIN_M762"] == 5) {
        new_Skin.M762 = 1101008067;
        new_Skin.M762_Mag = 1010080671;
    }
    if (Config["SKIN_M762"] == 6) {
        new_Skin.M762 = 1101008080;
        new_Skin.M762_Mag = 1010080801;
    }
    if (Config["SKIN_M762"] == 7) {
        new_Skin.M762 = 1101008093;
        new_Skin.M762_Mag = 1010080931;
    }
    if (Config["SKIN_M762"] == 8) {
        new_Skin.M762 = 1101008106;
        new_Skin.M762_Mag = 1010081061;
    }
    if (Config["SKIN_M762"] == 9) {
        new_Skin.M762 = 1101008119;
        new_Skin.M762_Mag = 1010081191;
    }

    // Additional weapon skins can be added here...
    // This structure makes it easy to add new weapons and skins
}

// ===========================================================================================================
// SKIN UTILITY FUNCTIONS
// ===========================================================================================================

// Get weapon skin ID by weapon name and skin index
int getWeaponSkinID(const std::string& weaponName, int skinIndex) {
    if (weaponName == "AKM") {
        switch(skinIndex) {
            case 0: return 101001;
            case 1: return 1101001089;
            case 2: return 1101001103;
            case 3: return 1101001116;
            case 4: return 1101001128;
            case 5: return 1101001143;
            case 6: return 1101001154;
            case 7: return 1101001174;
            case 8: return 1101001213;
            case 9: return 1101001231;
            case 10: return 1101001242;
            default: return 101001;
        }
    }
    // Add more weapons as needed
    return 0;
}

// Check if skin is available for weapon
bool isSkinAvailable(const std::string& weaponName, int skinIndex) {
    return getWeaponSkinID(weaponName, skinIndex) != 0;
}

// Get total number of skins for a weapon
int getTotalSkinsForWeapon(const std::string& weaponName) {
    if (weaponName == "AKM") return 11;
    if (weaponName == "KAR98K") return 7;
    if (weaponName == "AWM") return 8;
    if (weaponName == "M24") return 6;
    if (weaponName == "ACE32") return 3;
    if (weaponName == "VECTOR") return 5;
    if (weaponName == "UMP45") return 8;
    if (weaponName == "UZI") return 7;
    if (weaponName == "THOMPSON") return 3;
    if (weaponName == "M16A4") return 6;
    if (weaponName == "AUG") return 4;
    if (weaponName == "GROZAR") return 7;
    if (weaponName == "SCARL") return 8;
    if (weaponName == "M762") return 10;
    if (weaponName == "M249") return 5;
    if (weaponName == "DP28") return 5;
    return 1; // Default skin only
}

#endif // JOIN_SKIN_H