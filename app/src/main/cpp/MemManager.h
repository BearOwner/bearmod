#include <string>
#include "Helper/Includes.h"
#include "Substrate/CydiaSubstrate.h"

class MemManager
{
public:
    static long GetModuleBase(const char *moduleName);
    char *Shell(const char *cmd);
    int find_pid_of(const char *process_name);

    static long int preadv(int pid, void *buffer, ssize_t size, off_t off);
    long readValue(uintptr_t address, void *buffer, ssize_t size);/*32*/
    void writeValue(uintptr_t address, void *value, ssize_t size);
    long ReadDword32(long Address);/*32*/
    long ReadDword64(long Address);/*64*/
    long ReadInt32(long Address);
    float ReadFloat64(long Address);
    void getRoot(char **argv);
    char* getMac();
    bool isVPN();
    bool Anti_app(const char *packageName);
    void rm_app(const char *packageName);

    inline static int ReportKey(int fd, uint16_t type, uint16_t code, int32_t value);
    inline static void TouchPressDown(int fd, int FingerNum, int LineID, int x, int y);
    inline static void TouchMove(int fd, int FingerNum, int x, int y);
    inline static void TouchPressUp(int fd, int FingerNum);
    static int GetTouchEventNum();
    static int open_driver(const char * path, int flag);

    int getPID(char *PackageName);

    void AX(int a, const char *packageName);

    static int initPid();

    static bool ProcessRead(void *address, void *buffer, size_t size, bool write);

    static bool IsPtrValid(void *addr);

    bool Write(void *addr, void *buffer, size_t length);

    static bool Read(void *addr, void *buffer, size_t length);

    static bool PVM_ReadAddr(void *addr, void *buffer, size_t length);

    static bool PVM_WriteAddr(void *addr, void *buffer, size_t length);

    bool WriteAddr(void *addr, void *buffer, size_t length);

    bool WriteAddress(void *addr, void *buffer, size_t length);

    static void ProcessEvent(void *fn, void *parms);

    static uintptr_t FindPattern(const char *lib, const char *pattern);

    static uintptr_t GetEndAddress(const char *name);

    static void Hook(void *target, void *replace, void **backup);
};


template<typename T = uintptr_t>
T ReadType(uintptr_t addr) {
    T result{};
    if (MemManager::PVM_ReadAddr((void *) addr, &result, sizeof(T))) {
        return result;
    }
    return result;
}
template<typename T>
void Write2(uintptr_t addr, T value) {
    MemManager::PVM_WriteAddr((void *) addr, &value, sizeof(T));
}


//===================

