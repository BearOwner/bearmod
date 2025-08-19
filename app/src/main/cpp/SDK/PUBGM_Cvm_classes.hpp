#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class Cvm.CvmTestActor
// 0x0018 (0x0450 - 0x0438)
class ACvmTestActor : public AActor
{
public:
	class UStaticMeshComponent*                        CubeMesh;                                                 // 0x0438(0x0008) (Edit, BlueprintVisible, ExportObject, BlueprintReadOnly, ZeroConstructor, EditConst, InstancedReference, IsPlainOldData)
	unsigned char                                      UnknownData00[0x10];                                      // 0x0440(0x0010) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class Cvm.CvmTestActor");
		return pStaticClass;
	}

};


// Class Cvm.CvmTestCode
// 0x0008 (0x0030 - 0x0028)
class UCvmTestCode : public UObject
{
public:
	unsigned char                                      UnknownData00[0x8];                                       // 0x0028(0x0008) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class Cvm.CvmTestCode");
		return pStaticClass;
	}

};


}

