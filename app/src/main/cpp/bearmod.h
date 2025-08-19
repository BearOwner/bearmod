#pragma once

/*
 * BearMod Native Public Header
 * ----------------------------
 * Purpose:
 *   Acts as the stable, minimal public interface for native components used by the app.
 *   This header is part of a staged migration away from the large, monolithic NRG.h.
 *
 * Migration Plan (staged):
 *   1) Keep NRG.h included for backward compatibility (DEPRECATED) to avoid breaking builds.
 *   2) Gradually move only the symbols actually needed by other compilation units into this header
 *      as explicit declarations, documenting each as we go.
 *   3) Remove the raw NRG.h include when the migration completes.
 *
 * Threading/Lifecycle:
 *   Follows standard Android JNI threading rules. See per-function docs where provided.
 *
 * Note:
 *   Do NOT add broad includes here. Prefer forward declarations and per-file includes.
 */

// DEPRECATED: Temporary compatibility include. Do not rely on this long-term.
// TODO(bearmod/native): Remove this include once staged migration is complete.
#include "NRG.h"

// --- Minimal forward declarations (additive, documented as we migrate) ---

// Configuration dispatch used by Floating.onSendConfig JNI. Implemented in native codebase.
// Kept here to avoid pulling the entirety of NRG.h.
extern "C" void HandleOnSendConfig(const char* config, const char* value);

