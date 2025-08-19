#pragma once

// SDKGen by @xninjaleaks | @talhaeens 
// Send message for SDKGen and Offset Finder Tool

#include "../SDK.hpp"

namespace SDK
{
//---------------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------------

// Function ActorSequence.ActorSequenceComponent.StopPlay
struct UActorSequenceComponent_StopPlay_Params
{
};

// Function ActorSequence.ActorSequenceComponent.StartPlay
struct UActorSequenceComponent_StartPlay_Params
{
	float                                              StartTime;                                                // (Parm, ZeroConstructor, IsPlainOldData)
};

// Function ActorSequence.ActorSequenceComponent.OnStopOrFinsh
struct UActorSequenceComponent_OnStopOrFinsh_Params
{
};

// Function ActorSequence.ActorSequenceComponent.GetLength
struct UActorSequenceComponent_GetLength_Params
{
	float                                              ReturnValue;                                              // (Parm, OutParm, ZeroConstructor, ReturnParm, IsPlainOldData)
};

}

