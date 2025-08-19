#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Enums
//---------------------------------------------------------------------------

// Enum PlanPHRuntime.EDirectionType
enum class EDirectionType : uint8_t
{
	EDirectionType__Up             = 0,
	EDirectionType__Down           = 1,
	EDirectionType__Left           = 2,
	EDirectionType__Right          = 3,
	EDirectionType__EDirectionType_MAX = 4
};


// Enum PlanPHRuntime.EHomeAssetType
enum class EHomeAssetType : uint8_t
{
	EHomeAssetType__CompleteEdgeStyle = 0,
	EHomeAssetType__HalfEdgeStyle  = 1,
	EHomeAssetType__ArcStyle       = 2,
	EHomeAssetType__DoorStyle      = 3,
	EHomeAssetType__FilletWall     = 4,
	EHomeAssetType__EHomeAssetType_MAX = 5
};



//---------------------------------------------------------------------------
//Script Structs
//---------------------------------------------------------------------------

// ScriptStruct PlanPHRuntime.StructureData
// 0x0014
struct FStructureData
{
	struct FVector2D                                   Position;                                                 // 0x0000(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	float                                              Thickness;                                                // 0x0008(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	EHomeAssetType                                     AssetType;                                                // 0x000C(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	EDirectionType                                     Direction;                                                // 0x000D(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      OccupiedX;                                                // 0x000E(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      OccupiedY;                                                // 0x000F(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      PaintNum;                                                 // 0x0010(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0011(0x0003) MISSED OFFSET
};

// ScriptStruct PlanPHRuntime.PlanPH_CommonOccupy
// 0x0020
struct FPlanPH_CommonOccupy
{
	int                                                LandId;                                                   // 0x0000(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	bool                                               bOccupied;                                                // 0x0004(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0005(0x0003) MISSED OFFSET
	int                                                BeginTime;                                                // 0x0008(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	int                                                EndTime;                                                  // 0x000C(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	struct FString                                     PlayerUID;                                                // 0x0010(0x0010) (Edit, BlueprintVisible, ZeroConstructor)
};

}

