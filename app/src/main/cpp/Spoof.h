#include "Helper/Includes.h"
#include "Tools.h"
#include "Helper/fake_dlfcn.h"
#include "Helper/plthook.h"
// ============================================================================================= //
#define DefineHook(RET, NAME, ARGS) \
    RET (*Orig_ ## NAME) ARGS; \
    RET Hook_ ## NAME ARGS

#define HookRuntimeAPI(LIB, NAME) \
    LOGI("Hooking API: %s:%s (%p)", # LIB, # NAME, dlsym(dlopen(# LIB, 4), # NAME)); \
    Tools::Hook((void *) dlsym(dlopen(# LIB, 4), # NAME), (void *) &Hook_ ## NAME, (void **) &Orig_ ## NAME)
// ============================================================================================= //
char szPkgName[64];
const char *namesToHide[] = {"libErrolDec.so"};

#define ORIG_DATA_GL "cert_md5=9b99f18d642c4177f4d34c34ebcaec3a|apk_hash_1=|apk_hash_2=|txt_seg_crc="
#define ORIG_DATA_KR "cert_md5=9b99f18d642c4177f4d34c34ebcaec3a|apk_hash_1=|apk_hash_2=|txt_seg_crc="
#define ORIG_DATA_TW "cert_md5=9b99f18d642c4177f4d34c34ebcaec3a|apk_hash_1=|apk_hash_2=|txt_seg_crc="
#define ORIG_DATA_VN "cert_md5=9b99f18d642c4177f4d34c34ebcaec3a|apk_hash_1=|apk_hash_2=|txt_seg_crc="
#define ORIG_DATA_IN "cert_md5=7073b19e4dac8e00685530b43732f0db|apk_hash_1=|apk_hash_2=|txt_seg_crc=0xd8da69e3"

const char *isNameInBlackList(const char *in) {
    if (!in)
        return 0;

    for (int i = 0; i < (sizeof(namesToHide) / sizeof(namesToHide[0])); i++) {
        const char *found = strstr(in, namesToHide[i]);
        if (found) {
            return found;
        }
    }
    return 0;
}
// ============================================================================================= //
DefineHook(int, open, (const char *pathname, int flags, mode_t mode, void *unk1, void *unk2, void *unk3, void *unk4, void *unk5)) {
    if (isNameInBlackList(pathname)) {
        return -1;
    }

    if (strstr(pathname, szPkgName)) {
        if (strstr(pathname, ".apk")) {
            std::string s = "/data/data/";
            s += szPkgName;
            s += "/lib/libMTP.so";
            pathname = s.c_str();
        }
    }

    return Orig_open(pathname, flags, mode, unk1, unk2, unk3, unk4, unk5);
}

DefineHook(int, creat, (const char *pathname, int flags)) {
    if (isNameInBlackList(pathname)) {
        return -1;
    }

    if (strstr(pathname, szPkgName)) {
        if (strstr(pathname, ".apk")) {
            std::string s = "/data/data/";
            s += szPkgName;
            s += "/lib/libMTP.so";
            pathname = s.c_str();
        }
    }

    return Orig_creat(pathname, flags);
}
// ============================================================================================= //
DefineHook(FILE *, fopen, (const char* pathname, const char* mode)) {
    if (isNameInBlackList(pathname)) {
        return 0;
    }

    if (strstr(pathname, szPkgName)) {
        if (strstr(pathname, ".apk")) {
            std::string s = "/data/data/";
            s += szPkgName;
            s += "/lib/libMTP.so";
            pathname = s.c_str();
        }
    }

    return Orig_fopen(pathname, mode);
}// Created by @ErroldecOwner

// ============================================================================================= //
DefineHook(ssize_t, read, (int fd, void * buf, size_t count)) {
    auto result = Orig_read(fd, buf, count);

    if (result > 0) {
        uint8_t *buff = (uint8_t *) buf;
        for (int i = 0; i < result; i++) {
            for (int j = 0; j < (sizeof(namesToHide) / sizeof(namesToHide[0])); j++) {
                auto name = namesToHide[j];
                if (i <= (result - strlen(name))) {
                    if (memcmp(&buff[i], name, strlen(name)) == 0) {
                        memset(&buff[i], '.', strlen(name));
                    }
                }
            }
        }
    }
    return result;
}
// ============================================================================================= //
DefineHook(size_t, fread, (void * ptr, size_t size, size_t nmemb, FILE * stream)) {
    auto result = Orig_fread(ptr, size, nmemb, stream);

    if (result > 0) {
        uint8_t *buff = (uint8_t *) ptr;
        for (int i = 0; i < result; i++) {
            for (int j = 0; j < (sizeof(namesToHide) / sizeof(namesToHide[0])); j++) {
                auto name = namesToHide[j];
                if (i <= (result - strlen(name))) {
                    if (memcmp(&buff[i], name, strlen(name)) == 0) {
                        memset(&buff[i], '.', strlen(name));
                    }
                }
            }
        }
    }

    return result;
}
// ============================================================================================= //
DefineHook(int, access, (const char *pathname, int mode)) {
    if (isNameInBlackList(pathname)) {
        return -1;
    }
    return Orig_access(pathname, mode);
}
// ============================================================================================= //
DefineHook(int, stat, (const char *pathname, struct stat *statbuf)) {
    if (isNameInBlackList(pathname)) {
        return -1;
    }
    return Orig_stat(pathname, statbuf);
}

DefineHook(int, lstat, (const char *pathname, struct stat *statbuf)) {
    if (isNameInBlackList(pathname)) {
        return -1;
    }
    return Orig_lstat(pathname, statbuf);
}
// ============================================================================================= //
DefineHook(char *, fgets, (char * dest, int size, FILE * stream)) {
    auto ret = Orig_fgets(dest, size, stream);
    check:
    if (ret) {
        if (isNameInBlackList(ret)) {
            ret = Orig_fgets(dest, size, stream);
            goto check;
        }
    }

    return ret;
}
// ============================================================================================= //
DefineHook(DIR *, opendir, (const char *name)) {
    if (isNameInBlackList(name)) {
        return 0;
    }
    return Orig_opendir(name);
}

DefineHook(struct dirent *, readdir, (DIR * dirp)) {
    auto ret = Orig_readdir(dirp);
    check:
    if (ret) {
        if (isNameInBlackList(ret->d_name)) {
            ret = Orig_readdir(dirp);
            goto check;
        }
    }
    return ret;
}

// ============================================================================================= //
DefineHook(int, sprintf, (char * str, const char *format, ...)) {
    va_list args;
    va_start (args, format);
    int result = vsprintf(str, format, args);

    va_end(args);
    return result;
}

DefineHook(int, snprintf, (char * str, size_t size, const char *format, ...)) {
    va_list args;
    va_start (args, format);
    int result = vsnprintf(str, size, format, args);

    if (!strncmp(str, "cert_md5", 8)) {
        LOGI("%s", str);
        if (!strcmp(szPkgName, "com.tencent.ig")) {
            str = (char *) ORIG_DATA_GL;
        }
        if (!strcmp(szPkgName, "com.pubg.krmobile")) {
            str = (char *) ORIG_DATA_KR;
        }
        if (!strcmp(szPkgName, "com.rekoo.pubgm")) {
            str = (char *) ORIG_DATA_TW;
        }
        if (!strcmp(szPkgName, "com.vng.pubgmobile")) {
            str = (char *) ORIG_DATA_VN;
        }
        if (!strcmp(szPkgName, "com.pubg.imobile")) {
            str = (char *) ORIG_DATA_IN;
        }
    }

    va_end(args);
    return result;
}// Created by @ErroldecOwner

// ============================================================================================= //
DefineHook(int, syscall_wrapper, (int a1, void * a2, void * a3, void * a4, void * a5)) {
    if (a1 == 5 || a1 == 8 || a1 == 15 || a1 == 33 || a1 == 106 || a1 == 107 || a1 == 195 || a1 == 196) {
        const char *str = (const char *) a2;
        if (isNameInBlackList(str)) {
            return -1;
        }

        if (strstr(str, szPkgName)) {
            if (strstr(str, ".apk")) {
                std::string s = "/data/data/";
                s += szPkgName;
                s += "/lib/libMTP.so";
                a2 = (void *)s.c_str();
            }
        }
    }
    return Orig_syscall_wrapper(a1, a2, a3, a4, a5);
}

void *tersafe_hook(void *) {
    auto tersafe = Tools::GetBaseAddress("libtersafe.so");
    while (!tersafe) {
        tersafe = Tools::GetBaseAddress("libtersafe.so");
        sleep(1);
    }

    auto result = Tools::FindPattern("libtersafe.so", "07 49 79 44 08 60 07 48");
    if (result) {
        auto addr = result;
        while (true) {
            if (*(uint8_t*)(addr-2) == 0xC0 || *(uint8_t*)(addr-1) == 0x46) {
                break;
            } else addr++;
        }
        auto syscall_table = *(uintptr_t *) (addr) + result + 0x6;

        while (true) {
            *(uint32_t *) (syscall_table) = 0;
            sleep(1);
        }
    }

    return 0;
}// Created by @ErroldecOwner

// ============================================================================================= //
void StartRuntimeHook(const char *gamePkg) {
    LOGI("Starting Runtime Hook on %s", gamePkg);
    strcpy(szPkgName, gamePkg);

    HookRuntimeAPI(libc.so, open);
    //HookRuntimeAPI(libc.so, creat);
    HookRuntimeAPI(libc.so, read);
    //HookRuntimeAPI(libc.so, access);

    //HookRuntimeAPI(libc.so, stat);
    //HookRuntimeAPI(libc.so, lstat);

    HookRuntimeAPI(libc.so, fopen);
    HookRuntimeAPI(libc.so, fread);
    HookRuntimeAPI(libc.so, fgets);

    //HookRuntimeAPI(libc.so, opendir);
    //HookRuntimeAPI(libc.so, readdir);

    HookRuntimeAPI(libc.so, sprintf);
    HookRuntimeAPI(libc.so, snprintf);

    /*pthread_t t;
    pthread_create(&t, 0, tersafe_hook, 0);*/

    }

