#include "MD5.h"
#include <sstream>
#include <cstring>
#include <iomanip>

MD5::MD5() noexcept {
    init();
}

MD5::MD5(const std::string& text) {
    init();
    update(reinterpret_cast<const uint8_t*>(text.data()), text.size());
    finalize();
}

void MD5::init() noexcept {
    finalized = false;
    count = 0;
    state[0] = 0x67452301;
    state[1] = 0xefcdab89;
    state[2] = 0x98badcfe;
    state[3] = 0x10325476;
    buffer.fill(0);
    final_digest.fill(0);
}

void MD5::decode(uint32_t* output, const uint8_t* input, size_t len) noexcept {
    for (size_t i = 0, j = 0; j < len; i++, j += 4) {
        output[i] = static_cast<uint32_t>(input[j]) |
                    (static_cast<uint32_t>(input[j+1]) << 8) |
                    (static_cast<uint32_t>(input[j+2]) << 16) |
                    (static_cast<uint32_t>(input[j+3]) << 24);
    }
}

void MD5::encode(uint8_t* output, const uint32_t* input, size_t len) noexcept {
    for (size_t i = 0, j = 0; j < len; i++, j += 4) {
        output[j]   = static_cast<uint8_t>(input[i] & 0xff);
        output[j+1] = static_cast<uint8_t>((input[i] >> 8) & 0xff);
        output[j+2] = static_cast<uint8_t>((input[i] >> 16) & 0xff);
        output[j+3] = static_cast<uint8_t>((input[i] >> 24) & 0xff);
    }
}

void MD5::transform(const uint8_t block[BLOCK_SIZE]) noexcept {
    uint32_t a = state[0], b = state[1], c = state[2], d = state[3];
    uint32_t x[16];

    decode(x, block, BLOCK_SIZE);

    for (size_t i = 0; i < 64; ++i) {
        uint32_t f, g;

        if (i < 16) {
            f = F(b, c, d);
            g = i;
        } else if (i < 32) {
            f = G(b, c, d);
            g = (5*i + 1) % 16;
        } else if (i < 48) {
            f = H(b, c, d);
            g = (3*i + 5) % 16;
        } else {
            f = I(b, c, d);
            g = (7*i) % 16;
        }

        uint32_t temp = d;
        d = c;
        c = b;
        b += rotate_left(a + f + CONSTANTS[i] + x[g], SHIFTS[i]);
        a = temp;
    }

    state[0] += a;
    state[1] += b;
    state[2] += c;
    state[3] += d;
}

void MD5::update(const unsigned char* input, size_t length) noexcept {
    if (finalized) return;

    size_t index = static_cast<size_t>((count >> 3) & 0x3F);
    count += length << 3;

    size_t firstpart = BLOCK_SIZE - index;
    size_t i;

    if (length >= firstpart) {
        std::memcpy(&buffer[index], input, firstpart);
        transform(buffer.data());

        for (i = firstpart; i + BLOCK_SIZE <= length; i += BLOCK_SIZE) {
            transform(&input[i]);
        }

        index = 0;
    } else {
        i = 0;
    }

    std::memcpy(&buffer[index], &input[i], length - i);
}

void MD5::update(const char* input, size_t length) noexcept {
    update(reinterpret_cast<const unsigned char*>(input), length);
}

void MD5::finalize() noexcept {
    if (!finalized) {
        static constexpr std::array<uint8_t, BLOCK_SIZE> padding = {{
                                                                            0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                                                                    }};

        // Save number of bits
        std::array<uint8_t, 8> bits{};
        encode(bits.data(), reinterpret_cast<const uint32_t*>(&count), 8);

        // Pad out to 56 mod 64
        size_t index = static_cast<size_t>((count >> 3) & 0x3F);
        size_t padLen = (index < 56) ? (56 - index) : (120 - index);
        update(padding.data(), padLen);

        // Append length
        update(bits.data(), 8);

        // Store final digest
        encode(final_digest.data(), state, DIGEST_SIZE);

        // Secure wipe of sensitive data
        buffer.fill(0);
        count = 0;
        finalized = true;
    }
}

std::string MD5::hexdigest() const {
    if (!finalized) return "";

    std::stringstream ss;
    ss << std::hex << std::setfill('0');
    for (const auto& byte : final_digest) {
        ss << std::setw(2) << static_cast<int>(byte);
    }
    return ss.str();
}

std::array<uint8_t, 16> MD5::digest() const noexcept {
    return finalized ? final_digest : std::array<uint8_t, 16>{};
}

std::ostream& operator<<(std::ostream& os, const MD5& md5) {
    return os << md5.hexdigest();
}

// Utility function
std::string md5(const std::string& str) {
    MD5 hasher;
    hasher.update(str.data(), str.size());
    hasher.finalize();
    return hasher.hexdigest();
}