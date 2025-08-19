#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

namespace SDK
{
//---------------------------------------------------------------------------
//Classes
//---------------------------------------------------------------------------

// Class Renderer.SparseVolumeTextureViewerComponent
// 0x0030 (0x0860 - 0x0830)
class USparseVolumeTextureViewerComponent : public UPrimitiveComponent
{
public:
	float                                              Frame;                                                    // 0x0830(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	float                                              FrameRate;                                                // 0x0834(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      bPlaying : 1;                                             // 0x0838(0x0001) (Edit, BlueprintVisible)
	unsigned char                                      bLooping : 1;                                             // 0x0838(0x0001) (Edit, BlueprintVisible)
	unsigned char                                      bReversePlayback : 1;                                     // 0x0838(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly)
	unsigned char                                      bBlockingStreamingRequests : 1;                           // 0x0838(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly)
	unsigned char                                      bApplyPerFrameTransforms : 1;                             // 0x0838(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly)
	unsigned char                                      bPivotAtCentroid : 1;                                     // 0x0838(0x0001) (Edit, BlueprintVisible, BlueprintReadOnly)
	unsigned char                                      UnknownData00[0x3];                                       // 0x0839(0x0003) MISSED OFFSET
	float                                              VoxelSize;                                                // 0x083C(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	ESparseVolumeTexturePreviewAttribute               PreviewAttribute;                                         // 0x0840(0x0001) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData01[0x3];                                       // 0x0841(0x0003) MISSED OFFSET
	int                                                MipLevel;                                                 // 0x0844(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	float                                              Extinction;                                               // 0x0848(0x0004) (Edit, BlueprintVisible, ZeroConstructor, IsPlainOldData)
	unsigned char                                      UnknownData02[0x14];                                      // 0x084C(0x0014) MISSED OFFSET

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class Renderer.SparseVolumeTextureViewerComponent");
		return pStaticClass;
	}

};


// Class Renderer.SparseVolumeTextureViewer
// 0x0008 (0x0440 - 0x0438)
class ASparseVolumeTextureViewer : public AInfo
{
public:
	class USparseVolumeTextureViewerComponent*         SparseVolumeTextureViewerComponent;                       // 0x0438(0x0008) (Edit, BlueprintVisible, ExportObject, BlueprintReadOnly, ZeroConstructor, EditConst, InstancedReference, IsPlainOldData)

	static UClass* StaticClass()
	{
        static UClass *pStaticClass = 0;
        if (!pStaticClass)
            pStaticClass = UObject::FindClass("Class Renderer.SparseVolumeTextureViewer");
		return pStaticClass;
	}

};


}

