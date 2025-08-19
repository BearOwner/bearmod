#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class LightningComponent.LightningActor
// 0x0008 (0x0440 - 0x0438)
class ALightningActor : public AActor
{
public:
	class ULightningComponent*                         LightningComponent;                                       // 0x0438(0x0008) (Edit, BlueprintVisible, ExportObject, BlueprintReadOnly, ZeroConstructor, EditConst, InstancedReference, IsPlainOldData)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class LightningComponent.LightningActor");
		return pStaticClass;
	}

};


// Class LightningComponent.LightningComponent
// 0x00B0 (0x0920 - 0x0870)
class ULightningComponent : public UMeshComponent
{
public:
	float                                              LightningWidth;                                           // 0x0870(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	float                                              WidthDecay;                                               // 0x0874(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	float                                              BrightnessDecay;                                          // 0x0878(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	bool                                               bShrinkWidth;                                             // 0x087C(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x3];                                       // 0x087D(0x0003) MISSED OFFSET
	int                                                AtlasNum;                                                 // 0x0880(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	struct FVector                                     SeedStart;                                                // 0x0884(0x000C) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector                                     SeedEnd;                                                  // 0x0890(0x000C) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ZigZagFraction;                                           // 0x089C(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ZigZagDeviationRight;                                     // 0x08A4(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ZigZagDeviationUp;                                        // 0x08AC(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	float                                              ZigZagDeviationDecay;                                     // 0x08B4(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	struct FVector2D                                   ForkFraction;                                             // 0x08B8(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ForkZigZagDeviationRight;                                 // 0x08C0(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ForkZigZagDeviationUp;                                    // 0x08C8(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	float                                              ForkZigZagDeviationDecay;                                 // 0x08D0(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	struct FVector2D                                   ForkDeviationRight;                                       // 0x08D4(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ForkDeviationUp;                                          // 0x08DC(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	struct FVector2D                                   ForkDeviationForward;                                     // 0x08E4(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	float                                              ForkDeviationDecay;                                       // 0x08EC(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	struct FVector2D                                   ForkLength;                                               // 0x08F0(0x0008) (Edit, BlueprintVisible, IsPlainOldData)
	float                                              ForkLengthDecay;                                          // 0x08F8(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData01[0x24];                                      // 0x08FC(0x0024) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class LightningComponent.LightningComponent");
		return pStaticClass;
	}


	void SetWidthDecay(float InDecay);
	void SetShrinkWidth(bool InBool);
	void SetPatternMask(int InPatternMask);
	void SetMaxFractalTime(int InMaxFractalTime);
	void SetLightningWidth(float InWidth);
	void SetBrightnessDecay(float InDecay);
	void SetAtlasNum(int InVal);
	void RefreshLightningMesh();
	bool IsShrinkWidth();
	float GetWidthDecay();
	int GetPatternMask();
	int GetMaxFractalTime();
	float GetLightningWidth();
	float GetBrightnessDecay();
	float GetAtlasNum();
};


}

