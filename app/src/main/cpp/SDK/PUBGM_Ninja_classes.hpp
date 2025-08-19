#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class Ninja.BattleFieldCircle
// 0x00A8 (0x05A0 - 0x04F8)
class ABattleFieldCircle : public ALuaActor
{
public:
	unsigned char                                      UnknownData00[0x20];                                      // 0x04F8(0x0020) MISSED OFFSET
	float                                              CircleInterval;                                           // 0x0518(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	float                                              CurRadius;                                                // 0x051C(0x0004) (ZeroConstructor, IsPlainOldData)
	struct FName                                       OnlyCheckPlayerTag;                                       // 0x0520(0x0008) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	struct FString                                     LuaEventType;                                             // 0x0528(0x0010) (Edit, BlueprintVisible, ZeroConstructor)
	struct FString                                     LuaEventID;                                               // 0x0538(0x0010) (Edit, BlueprintVisible, ZeroConstructor)
	TMap<uint32_t, bool>                               PlayerKeyCache;                                           // 0x0548(0x0050) (ZeroConstructor)
	unsigned char                                      UnknownData01[0x8];                                       // 0x0598(0x0008) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class Ninja.BattleFieldCircle");
		return pStaticClass;
	}


	void StartMove(const struct FVector& StartCircle, const struct FVector& EndCircle, float MoveTime);
	void SetCircle(const struct FVector& circle);
	void OnRep_CircleMoveInfo();
	bool IsInBlueCircle(class AActor* uActor);
	void InitCircle(const struct FVector& InitCircle);
	TArray<uint32_t> GetAllPlayerInCircle();
	void CircleMove();
	void CheckInCircle();
};


}

