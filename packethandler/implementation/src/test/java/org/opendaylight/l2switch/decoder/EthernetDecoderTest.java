/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.sample.l2switch.md.topology;

import org.junit.Test;
import org.opendaylight.l2switch.decoder.EthernetDecoder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.types.rev140528.EthernetPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.types.rev140528.KnownEtherType;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class EthernetDecoderTest {

  @Test
  public void testDecode_IPv4EtherType() throws Exception {
    byte[] packet = {
      0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab,
      (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67,
      0x08, 0x00,
      0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11
    };
    EthernetPacket e = EthernetDecoder.decode(packet, 0);
    assertEquals(e.getEthertype(), KnownEtherType.Ipv4);
    assertNull(e.getLength());
    assertNull(e.getHeader8021q());
    assertEquals(e.getDestination().getValue(), "01:23:45:67:89:ab");
    assertEquals(e.getSource().getValue(), "cd:ef:01:23:45:67");
    assertTrue(Arrays.equals(e.getPayload(), Arrays.copyOfRange(packet, 14, packet.length)));
  }

  @Test
  public void testDecode_Length() throws Exception {
    byte[] packet = {
      0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab,
      (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67,
      0x00, 0x0e,
      0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11
    };
    EthernetPacket e = EthernetDecoder.decode(packet, 0);
    assertNull(e.getEthertype());
    assertEquals(e.getLength().intValue(), 14);
    assertNull(e.getHeader8021q());
    assertEquals(e.getDestination().getValue(), "01:23:45:67:89:ab");
    assertEquals(e.getSource().getValue(), "cd:ef:01:23:45:67");
    assertTrue(Arrays.equals(e.getPayload(), Arrays.copyOfRange(packet, 14, packet.length)));
  }

  @Test
  public void testDecode_IPv6EtherTypeWith8021qHeader() throws Exception {
    byte[] packet = {
      0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab,
      (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67,
      (byte)0x81, 0x00,
      (byte)0xff, (byte)0xff,
      (byte)0x86, (byte)0xdd,
      0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11
    };
    EthernetPacket e = EthernetDecoder.decode(packet, 0);
    assertEquals(e.getEthertype(), KnownEtherType.Ipv6);
    assertNull(e.getLength());
    assertEquals(e.getHeader8021q().size(), 1);
    assertEquals(e.getHeader8021q().get(0).getPriorityCode().intValue(), 7);
    assertTrue(e.getHeader8021q().get(0).isDropEligible());
    assertEquals(e.getHeader8021q().get(0).getVlan().intValue(), 4095);
    assertEquals(e.getDestination().getValue(), "01:23:45:67:89:ab");
    assertEquals(e.getSource().getValue(), "cd:ef:01:23:45:67");
    assertTrue(Arrays.equals(e.getPayload(), Arrays.copyOfRange(packet, 18, packet.length)));
  }
}