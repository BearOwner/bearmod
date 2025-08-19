#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class CreativeLua.CreativeBridgeLuaVM
// 0x0028 (0x0360 - 0x0338)
class UCreativeBridgeLuaVM : public UCreativeLuaVM
{
public:
	unsigned char                                      UnknownData00[0x8];                                       // 0x0338(0x0008) MISSED OFFSET
	struct FString                                     RegisterUGCVMFunctionHandlerName;                         // 0x0340(0x0010) (ZeroConstructor)
	struct FString                                     UGCVMPostInitFunctionHandlerName;                         // 0x0350(0x0010) (ZeroConstructor)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class CreativeLua.CreativeBridgeLuaVM");
		return pStaticClass;
	}


	void UGCLuaError(int errCode);
	void RegisterSluaCallUgcluaEventHandler();
	void PostInit();
};


}

