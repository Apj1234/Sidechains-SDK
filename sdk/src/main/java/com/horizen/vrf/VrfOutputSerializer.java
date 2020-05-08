package com.horizen.vrf;

import scorex.core.serialization.ScorexSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public class VrfOutputSerializer implements ScorexSerializer<VrfOutput> {
    private static VrfOutputSerializer serializer;

    static {
        serializer = new VrfOutputSerializer();
    }

    private VrfOutputSerializer() {
        super();
    }

    public static VrfOutputSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(VrfOutput vrfOutput, Writer writer) {
        writer.putInt(vrfOutput.bytes().length);
        writer.putBytes(vrfOutput.bytes());
    }

    @Override
    public VrfOutput parse(Reader reader) {
        int vrfOutputLen = reader.getInt();
        return VrfOutput.parse(reader.getBytes(vrfOutputLen));
    }
}
