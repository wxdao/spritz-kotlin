class Spritz {
    private var i = 0
    private var j = 0
    private var k = 0
    private var z = 0
    private var a = 0
    private var w = 1

    private val s: IntArray = {
        val array = IntArray(256)
        for (i in 0..255) {
            array[i] = i
        }
        array
    }()

    companion object n {
        fun crypt(data: ByteArray, key: ByteArray, iv: ByteArray? = null, inPlace: Boolean = true): ByteArray? {
            val obj = Spritz()
            obj.absorb(key)
            iv?.let {
                obj.absorbStop()
                obj.absorb(iv)
            }
            val sq = obj.squeeze(data.size)
            if (inPlace) {
                for (i in 0..data.size - 1) {
                    data[i] = (data[i].toInt() xor sq[i].toInt()).toByte()
                }
                return null
            } else {
                val res = ByteArray(data.size)
                for (i in 0..res.size - 1) {
                    res[i] = (data[i].toInt() xor sq[i].toInt()).toByte()
                }
                return res
            }
        }

        fun hash(data: ByteArray, bits: Int): ByteArray {
            val obj = Spritz()
            obj.absorb(data)
            obj.absorbStop()
            val r = (bits + 7) / 8
            obj.absorbByte(r.toByte())
            return obj.squeeze(r)
        }

        fun mac(data: ByteArray, key: ByteArray, bits: Int): ByteArray {
            val obj = Spritz()
            obj.absorb(key)
            obj.absorbStop()
            obj.absorb(data)
            obj.absorbStop()
            val r = (bits + 7) / 8
            obj.absorbByte(r.toByte())
            return obj.squeeze(r)
        }
    }

    private fun swap(a: Int, b: Int) {
        val tmp = s[a];
        s[a] = s[b]
        s[b] = tmp
    }

    private fun absorb(i: ByteArray) {
        for (b in i) {
            absorbByte(b)
        }
    }

    private fun absorbByte(b: Byte) {
        absorbNibble(b.toInt() and 0x0f);
        absorbNibble((b.toInt() and 0xff) shr 4);
    }

    private fun absorbNibble(x: Int) {
        if (x == 128) {
            shuffle()
        }
        swap(a++, 128 + x)
    }

    private fun absorbStop() {
        if (a++ == 128) {
            shuffle()
        }
    }

    private fun shuffle() {
        whip(512);
        crush();
        whip(512);
        crush();
        whip(512);
        a = 0;
    }

    private fun whip(r: Int) {
        for (v in 0..r - 1) {
            update()
        }
        w = (w + 2) and 0xff
    }

    private fun crush() {
        for (v in 0..127) {
            if (s[v] > s[255 - v]) {
                swap(v, 255 - v)
            }
        }
    }

    private fun squeeze(r: Int): ByteArray {
        if (a > 0) {
            shuffle()
        }
        val p = ByteArray(r)
        for (v in 0..r - 1) {
            p[v] = drip()
        }
        return p
    }

    private fun drip(): Byte {
        if (a > 0) {
            shuffle()
        }
        update()
        return output()
    }

    private fun update() {
        i = (i + w) and 0xff
        j = (k + s[(j + s[i]) and 0xff]) and 0xff
        k = (k + i + s[j]) and 0xff
        swap(i, j)
    }

    private fun output(): Byte {
        z = s[(j + s[(i + s[(z + k) and 0xff]) and 0xff]) and 0xff]
        return z.toByte()
    }
}
