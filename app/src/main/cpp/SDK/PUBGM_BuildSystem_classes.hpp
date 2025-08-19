#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class BuildSystem.BuildingActorBase
// 0x01C8 (0x06C0 - 0x04F8)
class ABuildingActorBase : public ALuaActor
{
public:
	unsigned char                                      UnknownData00[0x1C];                                      // 0x04F8(0x001C) MISSED OFFSET
	bool                                               bCustomBlockingChannels;                                  // 0x0514(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData01[0x3];                                       // 0x0515(0x0003) MISSED OFFSET
	TArray<TEnumAsByte<ECollisionChannel>>             CustomBlockingChannels;                                   // 0x0518(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	bool                                               bCheckVisibilitySkipTypes;                                // 0x0528(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bSkipCheckOwnerCollision;                                 // 0x0529(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData02[0x6];                                       // 0x052A(0x0006) MISSED OFFSET
	TArray<class UClass*>                              VisibilitySkipTypes;                                      // 0x0530(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	struct FBuildingActorWorldSnapSetup                WorldSnapSetup;                                           // 0x0540(0x0024) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance)
	float                                              DebugHealthDistance;                                      // 0x0564(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	struct FVector                                     DebugHealthOffset;                                        // 0x0568(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData03[0x4];                                       // 0x0574(0x0004) MISSED OFFSET
	TArray<class UClass*>                              ProhibitedActorTemplateList;                              // 0x0578(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	TArray<class UClass*>                              EnableBuildingList;                                       // 0x0588(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	struct FSoftObjectPath                             PreBuildingEffectPath;                                    // 0x0598(0x0018) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance)
	struct FVector                                     PreBuildingEffectScale;                                   // 0x05B0(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	bool                                               CanBuildUnderWater;                                       // 0x05BC(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bShouldSnapToWorldGrid;                                   // 0x05BD(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData04[0x2];                                       // 0x05BE(0x0002) MISSED OFFSET
	float                                              UnderWaterMaxBuildDepth;                                  // 0x05C0(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData05[0xC];                                       // 0x05C4(0x000C) MISSED OFFSET
	struct FTransform                                  DestroyedParticleTransformOffset;                         // 0x05D0(0x0030) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	struct FSoftObjectPath                             DestroyBuildingEffectPath;                                // 0x0600(0x0018) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance)
	bool                                               bUseExtraCenterOffset;                                    // 0x0618(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bUseExtraCenterRotation;                                  // 0x0619(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData06[0x2];                                       // 0x061A(0x0002) MISSED OFFSET
	struct FVector                                     ActorCollisionBoxExtern;                                  // 0x061C(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	struct FVector                                     ActorCollisionBoxCenter;                                  // 0x0628(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	struct FRotator                                    ActorCollisionBoxRotator;                                 // 0x0634(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	struct FVector                                     ViewLocationOffset;                                       // 0x0640(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	float                                              Health;                                                   // 0x064C(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, Net, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bDestroyWhenZeroHealth;                                   // 0x0650(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData07[0x7];                                       // 0x0651(0x0007) MISSED OFFSET
	struct FScriptMulticastDelegate                    OnHealthChangeNotify;                                     // 0x0658(0x0010) (ZeroConstructor, InstancedReference, BlueprintAssignable)
	float                                              MaxDeviation;                                             // 0x0668(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              MaxTraceDepth;                                            // 0x066C(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bDoQuadTrace;                                             // 0x0670(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	TEnumAsByte<EBuildingActorConstructingMode>        ConstructingMode;                                         // 0x0671(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bEnableOverlayPlace;                                      // 0x0672(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bEnablePitchRotatePlace;                                  // 0x0673(0x0001) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bAutoPickValidPlace;                                      // 0x0674(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData08[0x3];                                       // 0x0675(0x0003) MISSED OFFSET
	float                                              OverlayPlaceHeight;                                       // 0x0678(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              OverlayMaxHeightFromGround;                               // 0x067C(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              DetectDeath;                                              // 0x0680(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              MaxOffectHight;                                           // 0x0684(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bUseCachedBuildLocation;                                  // 0x0688(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData09[0x7];                                       // 0x0689(0x0007) MISSED OFFSET
	struct FBuildingActorDSBuildCheck                  DSBuildCheckCfg;                                          // 0x0690(0x0018) (Edit, BlueprintVisible)
	int                                                MaxCountLimit;                                            // 0x06A8(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bDoDensityCheck;                                          // 0x06AC(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData10[0x3];                                       // 0x06AD(0x0003) MISSED OFFSET
	struct FString                                     LuaModPath;                                               // 0x06B0(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class BuildSystem.BuildingActorBase");
		return pStaticClass;
	}


	static void SpawnDestroyParticle(bool bSpawnParticle, const struct FTransform& Loc, const struct FSoftObjectPath& DestroyBuildingEffectPath, class UWorld* World);
	void PlayDestroyAnimation(bool bUseParticle);
	void OnTakeDamageFromVehicle(class UPrimitiveComponent* MyPrimitiveComp, float ForwardSpeed, float DamageAmount, class AActor* DamageCauser, class AController* EventInstigator);
	void OnRep_Health();
	void OnPlayDestroyAnimation(bool bUseParticle);
	void OnBuildingActorDamaged(float Health);
	void OnBornAnimationPlayEndInClient();
	int GetBuildID();
	void BPOnOwnerChanged();
};


// Class BuildSystem.BuildSystemComponent
// 0x02A8 (0x0480 - 0x01D8)
class UBuildSystemComponent : public ULuaActorComponent
{
public:
	struct FScriptMulticastDelegate                    OnConstructionComplete;                                   // 0x01D8(0x0010) (ZeroConstructor, InstancedReference, BlueprintAssignable)
	struct FScriptMulticastDelegate                    OnDoubleClickMode2Event;                                  // 0x01E8(0x0010) (ZeroConstructor, InstancedReference, BlueprintAssignable)
	struct FScriptMulticastDelegate                    OnDoubleClickMode2PercentEvent;                           // 0x01F8(0x0010) (ZeroConstructor, InstancedReference, BlueprintAssignable)
	float                                              MaxmumConstructingDistance;                               // 0x0208(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	int                                                CurrentAvatarID;                                          // 0x020C(0x0004) (BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x78];                                      // 0x0210(0x0078) MISSED OFFSET
	class UClass*                                      BuildingSelectorClass;                                    // 0x0288(0x0008) (ZeroConstructor, Transient, IsPlainOldData)
	class UClass*                                      SelectBuildMeshClass;                                     // 0x0290(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              UpdateBuildEnableTimer;                                   // 0x0298(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              MinBuildDist;                                             // 0x029C(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              GridGroundThreshold;                                      // 0x02A0(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               AIIsOpenAdsorb;                                           // 0x02A4(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               AIIsOpenLeftAndRightTry;                                  // 0x02A5(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData01[0x2];                                       // 0x02A6(0x0002) MISSED OFFSET
	float                                              GridGroundCheckDepth;                                     // 0x02A8(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	struct FWorldGridData                              WorldGridData;                                            // 0x02AC(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance)
	bool                                               bCanPlaceOnConstructableActor;                            // 0x02B8(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData02[0x3];                                       // 0x02B9(0x0003) MISSED OFFSET
	int                                                bIsFastPlacementMode;                                     // 0x02BC(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	int                                                Mode2PressTouchBuildIndex;                                // 0x02C0(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bIsStartPreBuildMode2;                                    // 0x02C4(0x0001) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData03[0x3];                                       // 0x02C5(0x0003) MISSED OFFSET
	struct FVector2D                                   Mode2PreBuildPos;                                         // 0x02C8(0x0008) (Edit, BlueprintVisible, DisableEditOnInstance, IsPlainOldData)
	float                                              CurrentDoubleClickDuration;                               // 0x02D0(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              ValidDoubleClickDuration;                                 // 0x02D4(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              ValidDoubleClickInterval;                                 // 0x02D8(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              ValidDoubleClickDistance;                                 // 0x02DC(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bSouldSkipOwningPlayer;                                   // 0x02E0(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bNativeTouchActorBuildEnabled;                            // 0x02E1(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bNativeDoubleCkickBuildEnabled;                           // 0x02E2(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData04[0x5];                                       // 0x02E3(0x0005) MISSED OFFSET
	unsigned char                                      UnknownData05[0x50];                                      // 0x02E3(0x0050) UNKNOWN PROPERTY: MapProperty BuildSystem.BuildSystemComponent.ActorSelectorMap
	TArray<class UClass*>                              SkippingObjects;                                          // 0x0338(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	bool                                               bDebugDraw;                                               // 0x0348(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bPickLocationDebugDraw;                                   // 0x0349(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData06[0x6];                                       // 0x034A(0x0006) MISSED OFFSET
	TArray<class UClass*>                              FilterTemplates;                                          // 0x0350(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	TArray<class UClass*>                              ActorsShouldSkipVisiblityCheck;                           // 0x0360(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance)
	TEnumAsByte<ECollisionChannel>                     BuildingGridChannel;                                      // 0x0370(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData07[0x3];                                       // 0x0371(0x0003) MISSED OFFSET
	float                                              MaxCanAdsorbAngle;                                        // 0x0374(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              SnappingDistance;                                         // 0x0378(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              SnappingDetectRadius;                                     // 0x037C(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	float                                              AIMaxCanRotateAngle;                                      // 0x0380(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bCheckPlaceActorPosSwitch;                                // 0x0384(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData08[0x3];                                       // 0x0385(0x0003) MISSED OFFSET
	float                                              FloatErrorTolerance;                                      // 0x0388(0x0004) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	bool                                               bDSTraceCheck;                                            // 0x038C(0x0001) (Edit, BlueprintVisible, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData09[0x3];                                       // 0x038D(0x0003) MISSED OFFSET
	class ASelectBuildActor*                           SelectBuildActor;                                         // 0x0390(0x0008) (BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	EBuildingViewType                                  CurrentBuildViewType;                                     // 0x0398(0x0001) (ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData10[0x1F];                                      // 0x0399(0x001F) MISSED OFFSET
	int                                                CachedCDOIndex;                                           // 0x03B8(0x0004) (ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData11[0x4];                                       // 0x03BC(0x0004) MISSED OFFSET
	class AActor*                                      CachedCDOActor;                                           // 0x03C0(0x0008) (ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData12[0xB8];                                      // 0x03C8(0x00B8) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class BuildSystem.BuildSystemComponent");
		return pStaticClass;
	}


	void TryAttachToMoveablePlatform(class AActor* SpawnedBuilding, const struct FVector& BuildLocation);
	void StopPlaceBuilding();
	void StartPrePlaceBuilding(int InBuildID, int AvatarID, EBuildingViewType viewType);
	void SkipCDTimeByBuildID(int InBuildID, float IncreaseRate);
	bool ShouldEnableDoubleTouchMode();
	void SetPrebuildEnabled(bool CanBePlaced, bool IsVisible);
	void SetBuildingData(int Index, const struct FBuildingData& InData);
	bool ServerLineTraceCheck(class UClass* BuildingClass, const struct FVector& CheckLocation, const struct FRotator& CheckRotation);
	void ResetBuildngCDByBuildID(int InBuildID, float InNewCDTime);
	void ResetBuildList();
	bool ProccessNothingHitTraceOverlap(const struct FVector& DestLocation, const struct FRotator& DestRotation, float MaxBuildDist, int buildIndex, const struct FVector& ViewLocation, struct FVector* OutLocation);
	void PlaceBuildingWithIndex(int buildIndex, int AvatarID, bool bRelative);
	void PlaceBuildingAtLocation(int buildIndex, const struct FVector& Loc, const struct FRotator& Rot);
	void PlaceBuilding(int AvatarID, bool bRelative);
	bool PickLocationByBuildID(int InBuildID, struct FVector* OutLocation, struct FRotator* OutRotation);
	void OverriveDeploymentTransform(const struct FRotator& rotIn, const struct FVector& locIn, struct FRotator* rotOut, struct FVector* locOut);
	void OverrideBuildingMaxBuildDistance(float BuildingMaxDistance, bool SetAll, int BuildingID);
	void OnTouchedConstructableBoxEnded(TEnumAsByte<ETouchIndex> FingerIndex, class UPrimitiveComponent* TouchedComponent);
	void OnTouchedConstructableBox(TEnumAsByte<ETouchIndex> FingerIndex, class UPrimitiveComponent* TouchedComponent);
	void OnTouchActorBuild(const struct FVector2D& ScreenPosition, class APlayerController* Controller);
	void OnAsyncLoadingEffectFinished(int buildIndex);
	void OnAsyncLoadingBuildingFinished(int buildIndex);
	bool IsValidAvatar(int AvatarID);
	bool IsOverlayBuildHeightValid(const struct FVector& BuildLocation, float OverlayMaxHeightFromGround);
	bool IsInPreBuildingMode();
	bool IsCanPlaceBuildingBP(int InBuildID);
	bool IsCanPlaceBuilding(int InBuildID, TEnumAsByte<EBuildingActionType> _TYPE);
	bool IsActorProhibited(class AActor* Actor);
	struct FSoftObjectPath GetPreBuildingEffectPath(int InBuildID, int AvatarID);
	class APlayerController* GetOwnerPlayerController();
	float GetMaxDistance();
	bool GetIsHasInitData();
	int GetIndexByBuildingID(int BuildID);
	TEnumAsByte<EBuildingType> GetCurrentBuildType();
	class AActor* GetCDOByIndex(int Index);
	TArray<struct FBuildingData> GetBuildingList();
	void EnableBuildingByID(int BuildID, bool bEnable);
	void DoSceenTouchBuild(int buildIndex, const struct FVector2D& ScreenPostion, int PointerIndex, TEnumAsByte<EBuildingActionType> _TYPE, bool IsBegin, TEnumAsByte<EBuildingActionType> CustomBuildEvent);
	bool DensityCheck(const struct FBuildingActorDensityCheck& DensityCheckParmas);
	bool CheckShouldSkipByVisibility(class UPrimitiveComponent* _comp, int buildIndex);
	bool CheckPlacementWithPitch(const struct FHitResult& Hit, const struct FVector& BoxExtent, const struct FBuildingData& CurBuildData, struct FVector* OutLocation, struct FRotator* OutRotation);
	bool CheckPlacementOverlap(TArray<struct FHitResult> HitArray, const struct FVector& ViewLocation, const struct FVector& BoxExtent, const struct FBuildingData& CurBuildData, const struct FRotator& BuildRotation, const struct FRotator& ControlRot, struct FVector* OutLocation, bool* HasForbiddenObject);
	bool CheckPlaceBuildingWithIndex(int buildIndex);
	bool CheckObjectIsOneOfTheTemplate(class UObject* Obj, TArray<class UClass*> _ActorFilterTemplates);
	bool CheckCollisionNeedSkipOwner(int buildIndex);
	bool BuildAtWorldLoc(int buildIndex, const struct FTransform& tranx, const struct FVector& EndLocation, bool bUseTrace, TEnumAsByte<EBuildingActionType> BuildType);
	void AddBuildingData(const struct FBuildingData& InData);
};


// Class BuildSystem.BuildingActorInterface
// 0x0000 (0x0028 - 0x0028)
class UBuildingActorInterface : public UInterface
{
public:

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class BuildSystem.BuildingActorInterface");
		return pStaticClass;
	}


	bool UseCachedBuildLocation();
	bool ShouldUseExtraRotation();
	bool ShouldUseExtraOffset();
	bool ShouldSnapToGrid();
	bool ShouldSkipCheckOwnerCollision();
	bool ShouldCustomBlockingChannels();
	bool ShouldCheckVisibilityTypes();
	bool ShouldAttachToMovementPlatform();
	void SetBuildingActorID(int BuildID);
	void PrebuildCDOBodyInstance(class UWorld* World, const struct FTransform& tranx);
	void NonCullingBeginPlay();
	float MaxUnderWaterBuildDepth();
	bool IsEnablePitchRotatePlace();
	bool IsEnableOverlayPlace();
	bool IsAutoPickValidPlace();
	void HandleBuildingDestroyed(class AController* InstigatedBy);
	void HandleBuildingConstructed(class AController* InstigatedBy);
	struct FBuildingActorWorldSnapSetup GetWorldSnapSetup();
	TArray<class UClass*> GetVisibilitySkipTypes();
	struct FVector GetViewLocationOffset();
	TArray<class UClass*> GetProhibitedActorTemplateList();
	struct FVector GetPreBuildingEffectScale();
	struct FSoftObjectPath GetPreBuildingEffectPath();
	float GetOverlayPlaceHeight();
	float GetOverlayMaxHeightFromGround();
	float GetMaxTraceDepth();
	float GetMaxOffectHight();
	float GetMaxDeviation();
	struct FBuildingActorDSBuildCheck GetDSBuildCheckConfig();
	float GetDetectDeath();
	struct FTransform GetDestroyedParticleTransformOffset();
	struct FBuildingActorDensityCheck GetDensityParams(const struct FVector& Location);
	TArray<TEnumAsByte<ECollisionChannel>> GetCustomBlockingChannels();
	TEnumAsByte<EBuildingActorConstructingMode> GetConstructingMode();
	int GetBuildingActorID();
	struct FRotator GetActorCollisionBoxRotator();
	struct FVector GetActorCollisionBoxExtern();
	struct FVector GetActorCollisionBoxCenter();
	bool CanDoQuadTrace();
	bool CanBuildUnderWater();
	bool BPCheckPlacement(class UWorld* World, const struct FTransform& tranx, int CheckType, int CurrentAvatarID, class AActor* Builder);
};


// Class BuildSystem.BuildingActorMgr
// 0x0010 (0x0448 - 0x0438)
class ABuildingActorMgr : public AActor
{
public:
	TArray<struct FBuildingActorInfo>                  BuildingActorList;                                        // 0x0438(0x0010) (ZeroConstructor)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class BuildSystem.BuildingActorMgr");
		return pStaticClass;
	}


	void OnBuildingActorSpawned(class AActor* InOwnerActor, class ABuildingActorBase* InBuildingActor);
	void OnBuildingActorDestroyed(class ABuildingActorBase* InBuildingActor);
	static class ABuildingActorMgr* GetInstance(class UObject* WorldContextObject);
};


// Class BuildSystem.BuildingGridComponent
// 0x0020 (0x0870 - 0x0850)
class UBuildingGridComponent : public UBoxComponent
{
public:
	struct FName                                       CollisionProfileName;                                     // 0x0850(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, IsPlainOldData)
	struct FVector                                     BuildCenterOffset;                                        // 0x0858(0x000C) (Edit, BlueprintVisible, BlueprintReadOnly, DisableEditOnInstance, IsPlainOldData)
	unsigned char                                      UnknownData00[0xC];                                       // 0x0864(0x000C) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class BuildSystem.BuildingGridComponent");
		return pStaticClass;
	}

};


// Class BuildSystem.SelectBuildActor
// 0x0010 (0x0448 - 0x0438)
class ASelectBuildActor : public AActor
{
public:
	class UParticleSystemComponent*                    SelectBuildEffect;                                        // 0x0438(0x0008) (Edit, BlueprintVisible, ExportObject, BlueprintReadOnly, ZeroConstructor, DisableEditOnInstance, InstancedReference, IsPlainOldData)
	unsigned char                                      UnknownData00[0x8];                                       // 0x0440(0x0008) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class BuildSystem.SelectBuildActor");
		return pStaticClass;
	}


	void SetSelectActorTemplateScale(const struct FVector& inScale);
	void SetSelectActorTemplate(class UParticleSystem* Template);
	void SetSelectActorPlacementEnable(bool PlacementEnable, bool IsVisible);
	void SetIsPlacementEnable(bool Val);
	void OnParticleLoaded();
	bool IsCurrentPlacementEnable();
	bool GetIsPlacementEnable();
};


}

