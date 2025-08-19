#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class PixUILog.PxLogMgr
// 0x0010 (0x0038 - 0x0028)
class UPxLogMgr : public UObject
{
public:
	struct FScriptMulticastDelegate                    LogDelegate;                                              // 0x0028(0x0010) (ZeroConstructor, InstancedReference, BlueprintAssignable)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PixUILog.PxLogMgr");
		return pStaticClass;
	}


	static void PxLogToggleSwitchLevel(EPxLogLevels eLogLevel, bool beOpen);
	static void PxLogToggleSwitchGroup(EPxLogGroups eLogGroup, bool beOpen);
	static void PxLogEnable(bool beEnable);
	static class UPxLogMgr* Get();
	static void DispatchLog(EPxLogGroups eLogGroup, EPxLogLevels eLogLevel, const struct FString& strLogContent);
};


}

