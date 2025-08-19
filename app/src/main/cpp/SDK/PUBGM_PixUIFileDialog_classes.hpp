#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class PixUIFileDialog.PxFileDiaLogObj
// 0x0000 (0x0028 - 0x0028)
class UPxFileDiaLogObj : public UObject
{
public:

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class PixUIFileDialog.PxFileDiaLogObj");
		return pStaticClass;
	}

};


}

