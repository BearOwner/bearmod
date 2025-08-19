#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class PhotonBlast.PhotonReplicationStaticMeshComponent
// 0x00E0 (0x0B00 - 0x0A20)
class UPhotonReplicationStaticMeshComponent : public UStaticMeshComponent
{
public:
	unsigned char                                      UnknownData00[0x58];                                      // 0x0A20(0x0058) MISSED OFFSET
	int                                                ClusterUniqueID;                                          // 0x0A78(0x0004) (Edit, ZeroConstructor, DisableEditOnTemplate, EditConst, IsPlainOldData)
	bool                                               bCanMove;                                                 // 0x0A7C(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	EAOIEntityType                                     AOIEntityType;                                            // 0x0A7D(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	EClusterEntityState                                AOIEntityState;                                           // 0x0A7E(0x0001) (Edit, ZeroConstructor, DisableEditOnTemplate, EditConst, IsPlainOldData)
	unsigned char                                      ClusterReplicationOpen : 1;                               // 0x0A7F(0x0001) (Edit, BlueprintVisible)
	class UDestructionSubsystem*                       SubsystemPtr;                                             // 0x0A80(0x0008) (ZeroConstructor, Transient, IsPlainOldData)
	unsigned char                                      UnknownData01[0x8];                                       // 0x0A88(0x0008) MISSED OFFSET
	struct FLuaNetSerialization                        LuaNetSerialization;                                      // 0x0A90(0x0050) (Net)
	struct FString                                     LuaFilePath;                                              // 0x0AE0(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor)
	unsigned char                                      UnknownData02[0x10];                                      // 0x0AF0(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonReplicationStaticMeshComponent");
		return pStaticClass;
	}


	void UnRegistLuaTick();
	void UnRegisterFromCluster();
	void SetMoveable(bool Value);
	void SetClusterUniqueID(int InClusterID);
	void SetClusterEntityState(EClusterEntityState EntityState);
	void RegistLuaTick(float TickInterval);
	void RegisterToCluster();
	void MarkPropDirty(int PropIndex);
};


// Class PhotonBlast.PhotonDestructibleMeshComponent
// 0x0240 (0x0D40 - 0x0B00)
class UPhotonDestructibleMeshComponent : public UPhotonReplicationStaticMeshComponent
{
public:
	unsigned char                                      EnableImpactDamage : 1;                                   // 0x0B00(0x0001) (Edit)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0B01(0x0003) MISSED OFFSET
	struct FPhotonDestructibleImpactParam              ImpactParam;                                              // 0x0B04(0x0008) (Edit)
	unsigned char                                      SynchronizeChunkData : 1;                                 // 0x0B0C(0x0001) (Edit)
	unsigned char                                      UnknownData01[0x3];                                       // 0x0B0D(0x0003) MISSED OFFSET
	class UPhotonDestructibleMesh*                     PhotonDestructibleMesh;                                   // 0x0B10(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	TEnumAsByte<ECollisionEnabled>                     FragmentsCollisionEnabled;                                // 0x0B18(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData02[0x7];                                       // 0x0B19(0x0007) MISSED OFFSET
	struct FName                                       FragmentsCollisionProfileName;                            // 0x0B20(0x0008) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      InitialVisible : 1;                                       // 0x0B28(0x0001) (Edit)
	unsigned char                                      UnknownData03[0x7];                                       // 0x0B29(0x0007) MISSED OFFSET
	struct FPhotonHideReplicationData                  HideReplicationData;                                      // 0x0B30(0x0030) (Net)
	struct FPhotonDetachReplicationData                DetachReplicationData;                                    // 0x0B60(0x0030) (Net)
	struct FPhotonSlideReplicationData                 SlideReplicationData;                                     // 0x0B90(0x0028) (Net)
	float                                              FragmentsMaxHp;                                           // 0x0BB8(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      EnableCheckedSupportedFloor : 1;                          // 0x0BBC(0x0001) (Edit)
	unsigned char                                      EnableSeverImpactPoint : 1;                               // 0x0BBC(0x0001) (Edit)
	unsigned char                                      UnknownData04[0x183];                                     // 0x0BBD(0x0183) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonDestructibleMeshComponent");
		return pStaticClass;
	}


	void SetupFragmentsMaxHp(float HP);
	void SetServerDamagedDelegate(const struct FScriptDelegate& InDelegate);
	void SetFracturedMesh(class UPhotonDestructibleMesh* InFracturedMesh, bool Force);
	void Server_OnComponentHitAction(class UPrimitiveComponent* HitComp, class AActor* OtherActor, class UPrimitiveComponent* OtherComp, const struct FVector& NormalImpulse, const struct FHitResult& Hit);
	bool Server_DamageFragmentsByRadius(const struct FVector4& WorldImpactVelocityAndRotateStrength, const struct FVector4& WorldImpactPointAndSpreadStrength, float HP, float Radius, bool Attenuation);
	bool Server_DamageFragmentsByHp(TArray<int> FragmentsIndex, TArray<float> HP, const struct FVector4& WorldImpactPointAndSpreadStrength, const struct FVector4& WorldImpactVelocityAndRotateSpeed);
	void OnRep_SlideData();
	void OnRep_ImpactData();
	void OnRep_FragmentsState();
	bool IsFragmentCanDestroy(int FragmentItemIndex);
	bool IsFragmentCanBeDamaged(int FragmentItemIndex);
	class UPhotonDestructibleMesh* GetPhotonDestructibleMesh();
	bool GetFragmentTransform(int FragmentIndex, bool WorldSpace, struct FTransform* OutTransform);
	bool GetFragmentsWorldPosition(int FragmentIndex, struct FVector* FragmentPosition);
	bool GetFragmentsNotDamaged(bool IsReturnNotDestroyedFragments, TArray<int>* Fragments);
	bool GetFragmentsDamaged(TArray<int>* Fragments);
	bool GetFragmentsByRadius(const struct FVector& HitPoint, float Radius, int DamageType, TArray<int>* FragmentsIndex, TArray<float>* ImpactDistance);
	int GetFragmentItemCount();
	bool GetFragmentBounds(int FragmentIndex, bool WorldSpace, struct FBox* OutBox);
	class UPhotonFracturedMesh* GetFracturedMesh();
	void ClientDamageAndInitalFragments(TArray<int> DamagedFragmentItemIndex, TArray<int> InitialFragmentItemIndex);
};


// Class PhotonBlast.PhotonReplicationInstancedStaticMeshComponent
// 0x00E0 (0x0C50 - 0x0B70)
class UPhotonReplicationInstancedStaticMeshComponent : public UInstancedStaticMeshComponent
{
public:
	unsigned char                                      UnknownData00[0x58];                                      // 0x0B70(0x0058) MISSED OFFSET
	int                                                ClusterUniqueID;                                          // 0x0BC8(0x0004) (Edit, ZeroConstructor, DisableEditOnTemplate, EditConst, IsPlainOldData)
	bool                                               bCanMove;                                                 // 0x0BCC(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	EAOIEntityType                                     AOIEntityType;                                            // 0x0BCD(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	EClusterEntityState                                AOIEntityState;                                           // 0x0BCE(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      ClusterReplicationOpen : 1;                               // 0x0BCF(0x0001) (Edit, BlueprintVisible)
	class UDestructionSubsystem*                       SubsystemPtr;                                             // 0x0BD0(0x0008) (ZeroConstructor, Transient, IsPlainOldData)
	unsigned char                                      UnknownData01[0x8];                                       // 0x0BD8(0x0008) MISSED OFFSET
	struct FLuaNetSerialization                        LuaNetSerialization;                                      // 0x0BE0(0x0050) (Net)
	struct FString                                     LuaFilePath;                                              // 0x0C30(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor)
	unsigned char                                      UnknownData02[0x10];                                      // 0x0C40(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonReplicationInstancedStaticMeshComponent");
		return pStaticClass;
	}


	void UnRegistLuaTick();
	void UnRegisterFromCluster();
	void SetMoveable(bool Value);
	void SetClusterUniqueID(int InClusterID);
	void SetClusterEntityState(EClusterEntityState EntityState);
	void RegistLuaTick(float TickInterval);
	void RegisterToCluster();
	void MarkPropDirty(int PropIndex);
};


// Class PhotonBlast.PhotonInstancedDestructibleMeshComponent
// 0x01D0 (0x0E20 - 0x0C50)
class UPhotonInstancedDestructibleMeshComponent : public UPhotonReplicationInstancedStaticMeshComponent
{
public:
	unsigned char                                      EnableImpactDamage : 1;                                   // 0x0C50(0x0001) (Edit)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0C51(0x0003) MISSED OFFSET
	struct FPhotonDestructibleImpactParam              ImpactParam;                                              // 0x0C54(0x0008) (Edit)
	unsigned char                                      UnknownData01[0x4];                                       // 0x0C5C(0x0004) MISSED OFFSET
	class UPhotonDestructibleMesh*                     PhotonDestructibleMesh;                                   // 0x0C60(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	TEnumAsByte<ECollisionEnabled>                     FragmentsCollisionEnabled;                                // 0x0C68(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData02[0x7];                                       // 0x0C69(0x0007) MISSED OFFSET
	struct FName                                       FragmentsCollisionProfileName;                            // 0x0C70(0x0008) (Edit, ZeroConstructor, IsPlainOldData)
	TArray<struct FPhotonDestructibleFragmentStateData> HideInstanceReplicationData;                              // 0x0C78(0x0010) (Net, ZeroConstructor)
	TArray<struct FPhotonDestructibleImpactData>       DetachInstanceReplicationData;                            // 0x0C88(0x0010) (Net, ZeroConstructor)
	float                                              FragmentsMaxHp;                                           // 0x0C98(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      EnableCheckedSupportedFloor : 1;                          // 0x0C9C(0x0001) (Edit)
	unsigned char                                      EnableSeverImpactPoint : 1;                               // 0x0C9C(0x0001) (Edit)
	unsigned char                                      UnknownData03[0x183];                                     // 0x0C9D(0x0183) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonInstancedDestructibleMeshComponent");
		return pStaticClass;
	}


	void SetupFragmentsMaxHp(float HP);
	void SetServerDamagedDelegate(const struct FScriptDelegate& InDelegate);
	void SetPhysMaterialOverride(class UPhysicalMaterial* NewPhysMaterial);
	void SetFracturedMesh(class UPhotonDestructibleMesh* InFracturedMesh, bool Force);
	void Server_OnComponentHitAction(class UPrimitiveComponent* HitComp, class AActor* OtherActor, class UPrimitiveComponent* OtherComp, const struct FVector& NormalImpulse, const struct FHitResult& Hit);
	bool Server_DamageFragmentsByRadius(const struct FVector4& WorldImpactVelocity, const struct FVector4& WorldHitPointAndSpreadSpeed, float HP, float Radius, bool Attenuation);
	TArray<int> ReplaceAllInstances(TArray<struct FTransform> InstanceTransforms, bool bShouldReturnIndices);
	void OnRep_ImpactData();
	void OnRep_FragmentsState();
	bool IsFragmentCanDestroy(int InstanceIndex, int FragmentItemIndex);
	class UPhotonDestructibleMesh* GetPhotonDestructibleMesh();
	TArray<int> GetInstancesOverlappingSphere(const struct FVector& Center, float Radius, bool bSphereInWorldSpace);
	int GetInstanceItemCount();
	bool GetFragmentTransform(int InstanceIndex, int FragmentIndex, bool WorldSpace, struct FTransform* OutTransform);
	bool GetFragmentsWorldPosition(int InstanceIndex, int FragmentIndex, struct FVector* FragmentPosition);
	bool GetFragmentsNotDamaged(int InstanceIndex, bool IsReturnNotDestroyedFragments, TArray<int>* FragmentsNoDamaged);
	bool GetFragmentsDamaged(int InstanceIndex, TArray<int>* FragmentsDamaged);
	int GetFragmentItemCount();
	bool GetFragmentBounds(int InstanceIndex, int FragmentIndex, bool WorldSpace, struct FBox* OutBox);
	class UPhotonFracturedMesh* GetFracturedMesh();
	int AddInstanceWorldSpace(const struct FTransform& WorldTransform);
	TArray<int> AddInstances(TArray<struct FTransform> InstanceTransforms, bool bShouldReturnIndices, bool bMarkRenderStateDirty);
	int AddInstance(const struct FTransform& InstanceTransform);
};


// Class PhotonBlast.PhotonHierarchicalInstancedDestructibleMeshComponent
// 0x00E0 (0x0F00 - 0x0E20)
class UPhotonHierarchicalInstancedDestructibleMeshComponent : public UPhotonInstancedDestructibleMeshComponent
{
public:
	unsigned char                                      UnknownData00[0x10];                                      // 0x0E20(0x0010) MISSED OFFSET
	TArray<int>                                        SortedInstances;                                          // 0x0E30(0x0010) (ZeroConstructor)
	int                                                NumBuiltInstances;                                        // 0x0E40(0x0004) (ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData01[0x4];                                       // 0x0E44(0x0004) MISSED OFFSET
	struct FBox                                        BuiltInstanceBounds;                                      // 0x0E48(0x001C) (IsPlainOldData)
	struct FBox                                        UnbuiltInstanceBounds;                                    // 0x0E64(0x001C) (IsPlainOldData)
	TArray<struct FBox>                                UnbuiltInstanceBoundsList;                                // 0x0E80(0x0010) (ZeroConstructor)
	TArray<int>                                        UnbuiltInstanceIndexList;                                 // 0x0E90(0x0010) (ZeroConstructor)
	unsigned char                                      bEnableDensityScaling : 1;                                // 0x0EA0(0x0001)
	unsigned char                                      UnknownData02[0x27];                                      // 0x0EA1(0x0027) MISSED OFFSET
	int                                                OcclusionLayerNumNodes;                                   // 0x0EC8(0x0004) (ZeroConstructor, IsPlainOldData)
	struct FBoxSphereBounds                            CacheMeshExtendedBounds;                                  // 0x0ECC(0x001C) (IsPlainOldData)
	unsigned char                                      UnknownData03[0x8];                                       // 0x0EE8(0x0008) MISSED OFFSET
	int                                                MinInstancesToSplitNode;                                  // 0x0EF0(0x0004) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData04[0xC];                                       // 0x0EF4(0x000C) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonHierarchicalInstancedDestructibleMeshComponent");
		return pStaticClass;
	}


	bool ShowInstances(TArray<int> InstanceIndices, TArray<struct FTransform> InstanceTransforms);
	bool RemoveInstances(TArray<int> InstancesToRemove);
};


// Class PhotonBlast.PhotonDestructibleMeshActor
// 0x0010 (0x0448 - 0x0438)
class APhotonDestructibleMeshActor : public AActor
{
public:
	class UPhotonDestructibleMeshComponent*            PhotonDestructibleMeshComponent;                          // 0x0438(0x0008) (Edit, BlueprintVisible, ExportObject, BlueprintReadOnly, ZeroConstructor, EditConst, InstancedReference, IsPlainOldData)
	unsigned char                                      DestructibleMeshActorReplication : 1;                     // 0x0440(0x0001) (Edit, BlueprintVisible)
	unsigned char                                      UnknownData00[0x7];                                       // 0x0441(0x0007) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonDestructibleMeshActor");
		return pStaticClass;
	}

};


// Class PhotonBlast.ClusterReplicationVolume
// 0x0050 (0x04B8 - 0x0468)
class AClusterReplicationVolume : public AVolume
{
public:
	bool                                               bForReplication;                                          // 0x0468(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	bool                                               bForClusterDivide;                                        // 0x0469(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x6];                                       // 0x046A(0x0006) MISSED OFFSET
	struct FClusterAOIConfig                           ClusterConfig;                                            // 0x0470(0x0038) (Edit, BlueprintVisible)
	struct FClusterReplicationProxy                    ClusterReplication;                                       // 0x04A8(0x0010) (Net)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.ClusterReplicationVolume");
		return pStaticClass;
	}

};


// Class PhotonBlast.ClusterReplicationSelectVolume
// 0x0008 (0x0470 - 0x0468)
class AClusterReplicationSelectVolume : public AVolume
{
public:
	int16_t                                            LevelGroup;                                               // 0x0468(0x0002) (ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x6];                                       // 0x046A(0x0006) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.ClusterReplicationSelectVolume");
		return pStaticClass;
	}

};


// Class PhotonBlast.PhotonDestructibleMesh
// 0x00E0 (0x0310 - 0x0230)
class UPhotonDestructibleMesh : public UStaticMesh
{
public:
	class UPhotonFracturedMesh*                        FracturedMesh;                                            // 0x0230(0x0008) (ZeroConstructor, IsPlainOldData)
	TEnumAsByte<EPhotonDestructibleAction>             DestructibleAction;                                       // 0x0238(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UseDefaultParameter : 1;                                  // 0x0239(0x0001) (Edit)
	TEnumAsByte<EPhotonCollisionType>                  FragmentCollisonType;                                     // 0x023A(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x1];                                       // 0x023B(0x0001) MISSED OFFSET
	struct FPhotonDestructibleMeshPhysicsDetachData    PhysicsDetachData;                                        // 0x023C(0x0028) (Edit)
	unsigned char                                      UnknownData01[0xAC];                                      // 0x0264(0x00AC) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonDestructibleMesh");
		return pStaticClass;
	}

};


// Class PhotonBlast.DestructionSubsystem
// 0x0000 (0x05B8 - 0x05B8)
class UDestructionSubsystem : public UClusterReplicationSubsystem
{
public:

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.DestructionSubsystem");
		return pStaticClass;
	}

};


// Class PhotonBlast.PhotonFracturedMesh
// 0x00D8 (0x0100 - 0x0028)
class UPhotonFracturedMesh : public UObject
{
public:
	TArray<class UPhotonFracturedFragmentInfo*>        FracturedFragmentInfo;                                    // 0x0028(0x0010) (ZeroConstructor)
	TArray<int>                                        InsideMaterialIndex;                                      // 0x0038(0x0010) (Edit, ZeroConstructor)
	TMap<int, class UPhotonFracturedFragmentInfo*>     FragmentIndex2FragmentInfo;                               // 0x0048(0x0050) (ZeroConstructor)
	TMap<struct FName, class UPhotonFracturedFragmentInfo*> FragmentName2FragmentInfo;                                // 0x0098(0x0050) (ZeroConstructor)
	struct FSupportGraph                               SupportGraph;                                             // 0x00E8(0x0018)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonFracturedMesh");
		return pStaticClass;
	}

};


// Class PhotonBlast.PhotonFracturedMeshSettings
// 0x0000 (0x0028 - 0x0028)
class UPhotonFracturedMeshSettings : public UObject
{
public:

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonFracturedMeshSettings");
		return pStaticClass;
	}

};


// Class PhotonBlast.PhotonFEdgeData
// 0x0000 (0x0028 - 0x0028)
class UPhotonFEdgeData : public UObject
{
public:

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonFEdgeData");
		return pStaticClass;
	}

};


// Class PhotonBlast.PhotonFracturedFragmentInfo
// 0x0048 (0x0070 - 0x0028)
class UPhotonFracturedFragmentInfo : public UObject
{
public:
	int                                                FragmentItemIndex;                                        // 0x0028(0x0004) (Edit, ZeroConstructor, EditConst, IsPlainOldData)
	float                                              MassOverride;                                             // 0x002C(0x0004) (Edit, ZeroConstructor, EditConst, IsPlainOldData)
	struct FFragmentConvexElem                         ConvexElemForCollision;                                   // 0x0030(0x0010)
	struct FVector                                     centerPoint;                                              // 0x0040(0x000C) (IsPlainOldData)
	struct FBox                                        LocalBoundBox;                                            // 0x004C(0x001C) (IsPlainOldData)
	unsigned char                                      CanDestroy : 1;                                           // 0x0068(0x0001) (Edit)
	unsigned char                                      ConnectFloor : 1;                                         // 0x0068(0x0001) (Edit)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0069(0x0003) MISSED OFFSET
	int                                                ChunkIndex;                                               // 0x006C(0x0004) (Edit, ZeroConstructor, IsPlainOldData)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonFracturedFragmentInfo");
		return pStaticClass;
	}

};


// Class PhotonBlast.PhotonHierarchicalInstancedStaticMeshComponent
// 0x0110 (0x0D70 - 0x0C60)
class UPhotonHierarchicalInstancedStaticMeshComponent : public UHierarchicalInstancedStaticMeshComponent
{
public:
	class UStaticMesh*                                 EffectStaticMesh;                                         // 0x0C60(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	class USkeletalMesh*                               EffectSkeleMesh;                                          // 0x0C68(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	class UClass*                                      AnimClass;                                                // 0x0C70(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	class UClass*                                      EffectActorClass;                                         // 0x0C78(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	TArray<struct FPhotonInstanceImpactData>           InstanceImpactData;                                       // 0x0C80(0x0010) (Net, ZeroConstructor)
	float                                              InstanceMaxHp;                                            // 0x0C90(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	TEnumAsByte<EEffectType>                           EffectType;                                               // 0x0C94(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0C95(0x0003) MISSED OFFSET
	float                                              EffectDurationTime;                                       // 0x0C98(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData01[0x4];                                       // 0x0C9C(0x0004) MISSED OFFSET
	struct FName                                       EffectCollisionProfileName;                               // 0x0CA0(0x0008) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      bCullEffect : 1;                                          // 0x0CA8(0x0001) (Edit)
	unsigned char                                      UnknownData02[0x3];                                       // 0x0CA9(0x0003) MISSED OFFSET
	float                                              CullDistance;                                             // 0x0CAC(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      bUseExactVelocity : 1;                                    // 0x0CB0(0x0001) (Edit)
	unsigned char                                      UnknownData03[0x3];                                       // 0x0CB1(0x0003) MISSED OFFSET
	float                                              VelocityLength;                                           // 0x0CB4(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	float                                              MinVelocityExplosion;                                     // 0x0CB8(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	float                                              MaxVelocityExplosion;                                     // 0x0CBC(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	class UAkAudioEvent*                               HitSound;                                                 // 0x0CC0(0x0008) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData04[0x50];                                      // 0x0CC8(0x0050) UNKNOWN PROPERTY: SetProperty PhotonBlast.PhotonHierarchicalInstancedStaticMeshComponent.ExceptDamageTypes
	bool                                               bVehicleHitUseClientPreShow;                              // 0x0D18(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData05[0x3];                                       // 0x0D19(0x0003) MISSED OFFSET
	float                                              bVehicleHitScale;                                         // 0x0D1C(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	bool                                               bUpdateSurrendActorPosition;                              // 0x0D20(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	bool                                               bForceNetUpdate;                                          // 0x0D21(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData06[0x4E];                                      // 0x0D22(0x004E) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonHierarchicalInstancedStaticMeshComponent");
		return pStaticClass;
	}


	void UpdatePickupAndDeadBoxInRange(const struct FVector& Location);
	void SetFracturedMesh(class UPhotonDestructibleMesh* InPhotonDestructibleMesh);
	bool ServerGetInstanceIndexsNotDamaged(TArray<int>* InstancesNoDamaged);
	bool ServerGetInstanceIndexsDamaged(TArray<int>* InstancesDamaged);
	bool Server_InstanceByHp(TArray<int> InstancedIndex, TArray<float> DamagesHp, const struct FVector& WorldImpactVelocity, EDestructionDamageType DamageType);
	bool Server_InstanceAndWorldVelocityByHp(TArray<int> InstancedIndex, TArray<float> DamagesHp, const struct FVector& WorldImpactPoint, EDestructionDamageType DamageType, float Strength);
	bool Server_DamageFragmentsByRadius(const struct FVector& WorldImpactVelocity, const struct FVector& WorldHitPointAndSpreadSpeed, float HP, float Radius);
	void OnTriggerServerEvent__DelegateSignature(class ASTExtraVehicleBase* VehicleActor, class UPrimitiveComponent* HitComp, class UPhotonHierarchicalInstancedStaticMeshComponent* MeshComponent, const struct FVector& DesPos, int TriggerIndex);
	void OnTriggeredByComp(class AActor* OtherActor, class UPrimitiveComponent* OtherComp, const struct FTriggerEvent& TriggerEvent);
	void OnTriggerClientEvent__DelegateSignature(class ASTExtraVehicleBase* VehicleActor, class UPrimitiveComponent* HitComp, class UPhotonHierarchicalInstancedStaticMeshComponent* MeshComponent, const struct FVector& DesPos, int TriggerIndex);
	void OnRep_ImpactData();
	void OnLocalVehicleHit(TArray<int> InstanceIndex, const struct FVector4& WorldImpactPointAndSpreadSpeed, const struct FVector4& WorldImpactVelocityAndRotateSpeed);
	class UPhotonDestructibleMesh* GetPhotonDestructibleMesh();
	bool GetInstanceByRadius(const struct FVector& HitPoint, float Radius, EDestructionDamageType DamageType, TArray<int>* InstancedIndex);
	class UPhotonFracturedMesh* GetFracturedMesh();
	void GenerateEffectInstanceWorldSpace(TArray<int> InstanceIndex, TArray<struct FVector> WorldImpactVelocityAndRotateSpeed);
	void GenerateEffectInstanceLocalSpace(TArray<int> InstanceIndex, TArray<struct FVector> LocalImpactVelocityAndRotateSpeed);
	void ClientCorrectVehicleHit(TArray<int> InstanceIndex);
};


// Class PhotonBlast.PhotonReplicationSkeletalMeshComponent
// 0x00E0 (0x10C0 - 0x0FE0)
class UPhotonReplicationSkeletalMeshComponent : public USkeletalMeshComponent
{
public:
	unsigned char                                      UnknownData00[0x58];                                      // 0x0FE0(0x0058) MISSED OFFSET
	int                                                ClusterUniqueID;                                          // 0x1038(0x0004) (Edit, ZeroConstructor, DisableEditOnTemplate, EditConst, IsPlainOldData)
	bool                                               bCanMove;                                                 // 0x103C(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	EAOIEntityType                                     AOIEntityType;                                            // 0x103D(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	EClusterEntityState                                AOIEntityState;                                           // 0x103E(0x0001) (Edit, ZeroConstructor, DisableEditOnTemplate, EditConst, IsPlainOldData)
	unsigned char                                      ClusterReplicationOpen : 1;                               // 0x103F(0x0001) (Edit, BlueprintVisible)
	class UDestructionSubsystem*                       SubsystemPtr;                                             // 0x1040(0x0008) (ZeroConstructor, Transient, IsPlainOldData)
	unsigned char                                      UnknownData01[0x8];                                       // 0x1048(0x0008) MISSED OFFSET
	struct FLuaNetSerialization                        LuaNetSerialization;                                      // 0x1050(0x0050) (Net)
	struct FString                                     LuaFilePath;                                              // 0x10A0(0x0010) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor)
	unsigned char                                      UnknownData02[0x10];                                      // 0x10B0(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonReplicationSkeletalMeshComponent");
		return pStaticClass;
	}


	void UnRegistLuaTick();
	void UnRegisterFromCluster();
	void SetMoveable(bool Value);
	void SetClusterUniqueID(int InClusterID);
	void SetClusterEntityState(EClusterEntityState EntityState);
	void RegistLuaTick(float TickInterval);
	void RegisterToCluster();
	void MarkPropDirty(int PropIndex);
};


// Class PhotonBlast.PhotonStaticeMeshActor
// 0x0008 (0x0590 - 0x0588)
class APhotonStaticeMeshActor : public ADecoratorActor
{
public:
	unsigned char                                      UnknownData00[0x8];                                       // 0x0588(0x0008) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonStaticeMeshActor");
		return pStaticClass;
	}

};


// Class PhotonBlast.ReusableStaticMeshActor
// 0x0010 (0x0458 - 0x0448)
class AReusableStaticMeshActor : public AStaticMeshActor
{
public:
	unsigned char                                      UnknownData00[0x10];                                      // 0x0448(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.ReusableStaticMeshActor");
		return pStaticClass;
	}


	void OnSpawn();
	void OnRecycle();
};


// Class PhotonBlast.ReusableSkeletalMeshActor
// 0x0010 (0x04D0 - 0x04C0)
class AReusableSkeletalMeshActor : public ASkeletalMeshActor
{
public:
	unsigned char                                      UnknownData00[0x10];                                      // 0x04C0(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.ReusableSkeletalMeshActor");
		return pStaticClass;
	}


	void OnSpawn();
	void OnRecycle();
};


// Class PhotonBlast.ReusableDestructibleMeshActor
// 0x0010 (0x0458 - 0x0448)
class AReusableDestructibleMeshActor : public APhotonDestructibleMeshActor
{
public:
	unsigned char                                      UnknownData00[0x10];                                      // 0x0448(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.ReusableDestructibleMeshActor");
		return pStaticClass;
	}


	void OnSpawn();
	void OnRecycle();
};


// Class PhotonBlast.PhotonStaticMeshComponent
// 0x0030 (0x0A50 - 0x0A20)
class UPhotonStaticMeshComponent : public UStaticMeshComponent
{
public:
	class UStaticMesh*                                 EffectStaticMesh;                                         // 0x0A20(0x0008) (Edit, BlueprintVisible, BlueprintReadOnly, ZeroConstructor, IsPlainOldData)
	float                                              InstanceMaxHp;                                            // 0x0A28(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	TEnumAsByte<EEffectType>                           EffectType;                                               // 0x0A2C(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0A2D(0x0003) MISSED OFFSET
	float                                              EffectDurationTime;                                       // 0x0A30(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData01[0x4];                                       // 0x0A34(0x0004) MISSED OFFSET
	struct FName                                       EffectCollisionProfileName;                               // 0x0A38(0x0008) (Edit, ZeroConstructor, IsPlainOldData)
	bool                                               bVehicleHitUseClientPreShow;                              // 0x0A40(0x0001) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData02[0x3];                                       // 0x0A41(0x0003) MISSED OFFSET
	float                                              bVehicleHitScale;                                         // 0x0A44(0x0004) (Edit, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData03[0x8];                                       // 0x0A48(0x0008) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PhotonBlast.PhotonStaticMeshComponent");
		return pStaticClass;
	}


	void SetFracturedMesh(class UPhotonDestructibleMesh* InPhotonDestructibleMesh);
	class UPhotonDestructibleMesh* GetPhotonDestructibleMesh();
	class UPhotonFracturedMesh* GetFracturedMesh();
};


}

