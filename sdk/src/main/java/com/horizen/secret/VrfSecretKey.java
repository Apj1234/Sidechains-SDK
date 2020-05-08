package com.horizen.secret;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.horizen.proposition.ProofOfKnowledgeProposition;
import com.horizen.utils.Pair;
import com.horizen.vrf.VrfFunctions;
import com.horizen.vrf.VrfLoader;
import com.horizen.proof.VrfProof;
import com.horizen.proposition.VrfPublicKey;
import com.horizen.vrf.VrfOutput;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

import static com.horizen.secret.SecretsIdsEnum.VrfPrivateKeySecretId;
import static com.horizen.vrf.VrfFunctions.ProofType.VRF_PROOF;
import static com.horizen.vrf.VrfFunctions.ProofType.VRF_PROOF_OUTPUT;

public class VrfSecretKey implements Secret {
    private final byte[] secretBytes;
    private final byte[] publicBytes;

    public VrfSecretKey(byte[] secretKey, byte[] publicKey) {
        Objects.requireNonNull(secretKey, "Secret key can't be null");
        Objects.requireNonNull(publicKey, "Public key can't be null");

        secretBytes =  Arrays.copyOf(secretKey, secretKey.length);
        publicBytes = Arrays.copyOf(publicKey, publicKey.length);
    }

    private byte[] getSecretBytes() {
        return Arrays.copyOf(secretBytes, secretBytes.length);
    }

    public byte[] getPublicBytes() {
        return publicBytes;
    }

    public Pair<VrfProof, VrfOutput> prove(byte[] message) {
        EnumMap<VrfFunctions.ProofType, byte[]> proofs = VrfLoader.vrfFunctions().createVrfProof(getSecretBytes(), getPublicBytes(), message);
        return new Pair<>(new VrfProof(proofs.get(VRF_PROOF)), new VrfOutput(proofs.get(VRF_PROOF_OUTPUT)));
    }

    @Override
    public byte secretTypeId() {
        return VrfPrivateKeySecretId.id();
    }

    @Override
    public VrfPublicKey publicImage() {
        byte[] publicKey = Arrays.copyOf(publicBytes, publicBytes.length);
        return new VrfPublicKey(publicKey);
    }

    @Override
    public byte[] bytes() {
        int secretLength = secretBytes.length;
        return Bytes.concat(Ints.toByteArray(secretLength), secretBytes, publicBytes);
    }

    public static VrfSecretKey parse(byte[] bytes) {
        int secretKeyOffset = Ints.BYTES;
        int secretKeyLength = Ints.fromByteArray(Arrays.copyOfRange(bytes, 0, secretKeyOffset));
        int publicKeyOffset = secretKeyOffset + secretKeyLength;

        byte[] secretKey = Arrays.copyOfRange(bytes, secretKeyOffset, publicKeyOffset);
        byte[] publicKey = Arrays.copyOfRange(bytes, publicKeyOffset, bytes.length);

        return new VrfSecretKey(secretKey, publicKey);
    }

    @Override
    public SecretSerializer serializer() {
        return VrfSecretKeySerializer.getSerializer();
    }

    @Override
    public boolean owns(ProofOfKnowledgeProposition proposition) {
        return Arrays.equals(publicBytes, proposition.pubKeyBytes());
    }

    @Override
    public VrfProof sign(byte[] message) {
        return prove(message).getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VrfSecretKey that = (VrfSecretKey) o;
        return Arrays.equals(secretBytes, that.secretBytes) &&
                Arrays.equals(publicBytes, that.publicBytes);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(secretBytes);
        result = 31 * result + Arrays.hashCode(publicBytes);
        return result;
    }
}
