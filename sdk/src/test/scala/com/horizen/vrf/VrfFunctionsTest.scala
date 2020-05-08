package com.horizen.vrf

import java.util

import com.horizen.vrf.VrfFunctions.{KeyType, ProofType}
import org.junit.Assert.{assertEquals, assertFalse, assertNotEquals, assertTrue}
import org.junit.Test

import scala.util.Random


class VrfFunctionsTest {
  val keys: util.EnumMap[KeyType, Array[Byte]] = VrfLoader.vrfFunctions.generatePublicAndSecretKeys(1.toString.getBytes())
  val secretBytes: Array[Byte] = keys.get(KeyType.SECRET)
  val publicBytes: Array[Byte] = keys.get(KeyType.PUBLIC)
  val message: Array[Byte] = "Very secret message!".getBytes
  val vrfProofBytes: Array[Byte] = VrfLoader.vrfFunctions.createVrfProof(secretBytes, publicBytes, message).get(ProofType.VRF_PROOF)
  val vrfProofCheck: Boolean = VrfLoader.vrfFunctions.verifyProof(message, publicBytes, vrfProofBytes)
  val vrfOutputBytes: Array[Byte] = VrfLoader.vrfFunctions.vrfProofToVrfOutput(publicBytes, message, vrfProofBytes).get()

  @Test
  def sanityCheck(): Unit = {
    assertNotEquals(vrfProofBytes.deep, vrfOutputBytes.deep)
    assertTrue(VrfLoader.vrfFunctions.publicKeyIsValid(publicBytes))
    assertTrue(vrfProofCheck)
    assertTrue(vrfOutputBytes.nonEmpty)
  }


  @Test
  def determinismCheck(): Unit = {
    //@TODO add seed check here as it became supported
    val rnd = new Random()

    for (i <- 1 to 10) {
      val messageLen = rnd.nextInt(128) % VrfLoader.vrfFunctions.maximumVrfMessageLength()
      val newMessage = rnd.nextString(rnd.nextInt(128)).getBytes.take(messageLen)
      val firstVrfProofBytes = VrfLoader.vrfFunctions.createVrfProof(secretBytes, publicBytes, newMessage).get(ProofType.VRF_PROOF)
      val secondVrfProofBytes = VrfLoader.vrfFunctions.createVrfProof(secretBytes, publicBytes, newMessage).get(ProofType.VRF_PROOF)
      //assertEquals(vrfProofBytes.deep, otherVrfProofBytes.deep)

      val firstVrfOutputBytes = VrfLoader.vrfFunctions.vrfProofToVrfOutput(publicBytes, newMessage, firstVrfProofBytes).get
      val secondVrfOutputBytes = VrfLoader.vrfFunctions.vrfProofToVrfOutput(publicBytes, newMessage, secondVrfProofBytes).get

      assertEquals(firstVrfOutputBytes.deep, secondVrfOutputBytes.deep)
      println(s"Vrf output determinism check: iteration ${i}, for message len ${newMessage.length}")
    }
  }

  @Test()
  def tryToCorruptProof(): Unit= {
    val corruptedMessage: Array[Byte] = "Not very secret message!".getBytes
    val vrfProofCheckCorruptedMessage = VrfLoader.vrfFunctions.verifyProof(corruptedMessage, publicBytes, vrfProofBytes)
    assertFalse(vrfProofCheckCorruptedMessage)

    val corruptedProofBytes: Array[Byte] = util.Arrays.copyOf(vrfProofBytes, vrfProofBytes.length)
    corruptedProofBytes(0) = (~corruptedProofBytes(0)).toByte
    val vrfProofCheckCorruptedVrfProof = VrfLoader.vrfFunctions.verifyProof(message, publicBytes, corruptedProofBytes)
    assertFalse(vrfProofCheckCorruptedVrfProof)

    val corruptedPublicBytes: Array[Byte] = util.Arrays.copyOf(publicBytes, publicBytes.length)
    corruptedPublicBytes(0) = (~corruptedPublicBytes(0)).toByte
    val vrfProofCheckCorruptedPublicBytes = VrfLoader.vrfFunctions.verifyProof(message, corruptedPublicBytes, vrfProofBytes)
    assertFalse(vrfProofCheckCorruptedPublicBytes)
  }

}