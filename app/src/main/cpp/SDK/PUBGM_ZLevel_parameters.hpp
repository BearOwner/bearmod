#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

#include "../SDK.hpp"

namespace SDK
{
//---------------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------------

// Function ZLevel.ZLevelData.ReBindLevelDataComponent
struct AZLevelData_ReBindLevelDataComponent_Params
{
};

// Function ZLevel.ZLevelData.CheckMonsterSpotIsOnLand
struct AZLevelData_CheckMonsterSpotIsOnLand_Params
{
	class UZMonsterSpot*                               MonsterSpot;                                              // (Parm, ZeroConstructor, InstancedReference, IsPlainOldData)
	class UZMonsterSpotGroup*                          SpotGroup;                                                // (Parm, ZeroConstructor, InstancedReference, IsPlainOldData)
};

}

