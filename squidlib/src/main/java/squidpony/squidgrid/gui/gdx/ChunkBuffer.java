package squidpony.squidgrid.gui.gdx;

import squidpony.annotation.GwtIncompatible;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

/**
 * Created by Tommy Ettinger on 1/28/2020.
 */
@GwtIncompatible
class ChunkBuffer extends DataOutputStream {
	final ByteArrayOutputStream buffer;
	final CRC32 crc;

	ChunkBuffer (int initialSize) {
		this(new ByteArrayOutputStream(initialSize), new CRC32());
	}

	private ChunkBuffer (ByteArrayOutputStream buffer, CRC32 crc) {
		super(new CheckedOutputStream(buffer, crc));
		this.buffer = buffer;
		this.crc = crc;
	}

	public void endChunk (DataOutputStream target) throws IOException {
		flush();
		target.writeInt(buffer.size() - 4);
		buffer.writeTo(target);
		target.writeInt((int)crc.getValue());
		buffer.reset();
		crc.reset();
	}
}
