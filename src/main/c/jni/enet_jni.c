#include <jni.h>
#include <string.h>
#include <stdlib.h>

// Define ENET_IMPLEMENTATION before including enet.h to include the implementation
#define ENET_IMPLEMENTATION
#include "../enet.h"

// Helper functions for working with Java objects

// Convert ENetAddress to Java ENetAddress
static void setENetAddressToJava(JNIEnv *env, jobject jAddress, const ENetAddress *address) {
    jclass addressClass = (*env)->GetObjectClass(env, jAddress);
    
    // Get field IDs
    jfieldID hostField = (*env)->GetFieldID(env, addressClass, "host", "[B");
    jfieldID portField = (*env)->GetFieldID(env, addressClass, "port", "S");
    jfieldID scopeIdField = (*env)->GetFieldID(env, addressClass, "scopeId", "S");
    
    // Set host field (byte array)
    jbyteArray hostArray = (jbyteArray)(*env)->GetObjectField(env, jAddress, hostField);
    (*env)->SetByteArrayRegion(env, hostArray, 0, 16, (jbyte*)&address->host);
    
    // Set port and scopeId fields
    (*env)->SetShortField(env, jAddress, portField, address->port);
    (*env)->SetShortField(env, jAddress, scopeIdField, address->sin6_scope_id);
}

// Convert Java ENetAddress to ENetAddress
static void getENetAddressFromJava(JNIEnv *env, jobject jAddress, ENetAddress *address) {
    if (jAddress == NULL) {
        memset(address, 0, sizeof(ENetAddress));
        return;
    }
    
    jclass addressClass = (*env)->GetObjectClass(env, jAddress);
    
    // Get field IDs
    jfieldID hostField = (*env)->GetFieldID(env, addressClass, "host", "[B");
    jfieldID portField = (*env)->GetFieldID(env, addressClass, "port", "S");
    jfieldID scopeIdField = (*env)->GetFieldID(env, addressClass, "scopeId", "S");
    
    // Get host field (byte array)
    jbyteArray hostArray = (jbyteArray)(*env)->GetObjectField(env, jAddress, hostField);
    (*env)->GetByteArrayRegion(env, hostArray, 0, 16, (jbyte*)&address->host);
    
    // Get port and scopeId fields
    address->port = (*env)->GetShortField(env, jAddress, portField);
    address->sin6_scope_id = (*env)->GetShortField(env, jAddress, scopeIdField);
}

// Set ENetEvent fields in Java ENetEvent object
static void setENetEventToJava(JNIEnv *env, jobject jEvent, const ENetEvent *event) {
    jclass eventClass = (*env)->GetObjectClass(env, jEvent);
    
    // Get field IDs
    jfieldID typeField = (*env)->GetFieldID(env, eventClass, "type", "I");
    jfieldID peerField = (*env)->GetFieldID(env, eventClass, "peer", "J");
    jfieldID channelIDField = (*env)->GetFieldID(env, eventClass, "channelID", "I");
    jfieldID dataField = (*env)->GetFieldID(env, eventClass, "data", "I");
    jfieldID packetField = (*env)->GetFieldID(env, eventClass, "packet", "J");
    
    // Set fields
    (*env)->SetIntField(env, jEvent, typeField, event->type);
    (*env)->SetLongField(env, jEvent, peerField, (jlong)event->peer);
    (*env)->SetIntField(env, jEvent, channelIDField, event->channelID);
    (*env)->SetIntField(env, jEvent, dataField, event->data);
    (*env)->SetLongField(env, jEvent, packetField, (jlong)event->packet);
}

// JNI method implementations

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    initialize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetLibrary_initialize
  (JNIEnv *env, jclass cls) {
    return enet_initialize();
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    deinitialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_deinitialize
  (JNIEnv *env, jclass cls) {
    enet_deinitialize();
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    hostCreate
 * Signature: (Llogykk/enet/ENetAddress;IIII)J
 */
JNIEXPORT jlong JNICALL Java_logykk_enet_ENetLibrary_hostCreate
  (JNIEnv *env, jclass cls, jobject jAddress, jint peerCount, jint channelLimit, jint incomingBandwidth, jint outgoingBandwidth) {
    ENetAddress address;
    ENetHost *host;
    
    if (jAddress != NULL) {
        getENetAddressFromJava(env, jAddress, &address);
        host = enet_host_create(&address, peerCount, channelLimit, incomingBandwidth, outgoingBandwidth);
    } else {
        host = enet_host_create(NULL, peerCount, channelLimit, incomingBandwidth, outgoingBandwidth);
    }
    
    return (jlong)host;
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    hostDestroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_hostDestroy
  (JNIEnv *env, jclass cls, jlong host) {
    enet_host_destroy((ENetHost*)host);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    hostConnect
 * Signature: (JLlogykk/enet/ENetAddress;II)J
 */
JNIEXPORT jlong JNICALL Java_logykk_enet_ENetLibrary_hostConnect
  (JNIEnv *env, jclass cls, jlong host, jobject jAddress, jint channelCount, jint data) {
    ENetAddress address;
    getENetAddressFromJava(env, jAddress, &address);
    
    ENetPeer *peer = enet_host_connect((ENetHost*)host, &address, channelCount, data);
    return (jlong)peer;
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    hostService
 * Signature: (JLlogykk/enet/ENetEvent;I)I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetLibrary_hostService
  (JNIEnv *env, jclass cls, jlong host, jobject jEvent, jint timeout) {
    ENetEvent event;
    int result = enet_host_service((ENetHost*)host, &event, timeout);
    
    if (result > 0) {
        setENetEventToJava(env, jEvent, &event);
    }
    
    return result;
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    hostFlush
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_hostFlush
  (JNIEnv *env, jclass cls, jlong host) {
    enet_host_flush((ENetHost*)host);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    packetCreate
 * Signature: ([BII)J
 */
JNIEXPORT jlong JNICALL Java_logykk_enet_ENetLibrary_packetCreate
  (JNIEnv *env, jclass cls, jbyteArray jData, jint dataLength, jint flags) {
    jbyte *data = (*env)->GetByteArrayElements(env, jData, NULL);
    
    ENetPacket *packet = enet_packet_create(data, dataLength, flags);
    
    (*env)->ReleaseByteArrayElements(env, jData, data, JNI_ABORT);
    
    return (jlong)packet;
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    packetDestroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_packetDestroy
  (JNIEnv *env, jclass cls, jlong packet) {
    enet_packet_destroy((ENetPacket*)packet);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    hostBroadcast
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_hostBroadcast
  (JNIEnv *env, jclass cls, jlong host, jint channelID, jlong packet) {
    enet_host_broadcast((ENetHost*)host, channelID, (ENetPacket*)packet);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerSend
 * Signature: (JIJ)I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetLibrary_peerSend
  (JNIEnv *env, jclass cls, jlong peer, jint channelID, jlong packet) {
    return enet_peer_send((ENetPeer*)peer, channelID, (ENetPacket*)packet);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerDisconnect
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_peerDisconnect
  (JNIEnv *env, jclass cls, jlong peer, jint data) {
    enet_peer_disconnect((ENetPeer*)peer, data);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerDisconnectLater
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_peerDisconnectLater
  (JNIEnv *env, jclass cls, jlong peer, jint data) {
    enet_peer_disconnect_later((ENetPeer*)peer, data);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerDisconnectNow
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_logykk_enet_ENetLibrary_peerDisconnectNow
  (JNIEnv *env, jclass cls, jlong peer, jint data) {
    enet_peer_disconnect_now((ENetPeer*)peer, data);
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerGetAddress
 * Signature: (J)Llogykk/enet/ENetAddress;
 */
JNIEXPORT jobject JNICALL Java_logykk_enet_ENetLibrary_peerGetAddress
  (JNIEnv *env, jclass cls, jlong peer) {
    // Create a new ENetAddress Java object
    jclass addressClass = (*env)->FindClass(env, "logykk/enet/ENetAddress");
    jmethodID constructor = (*env)->GetMethodID(env, addressClass, "<init>", "()V");
    jobject jAddress = (*env)->NewObject(env, addressClass, constructor);
    
    // Get the address from the peer
    ENetAddress *address = &((ENetPeer*)peer)->address;
    
    // Set the address fields in the Java object
    setENetAddressToJava(env, jAddress, address);
    
    return jAddress;
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerGetRoundTripTime
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetLibrary_peerGetRoundTripTime
  (JNIEnv *env, jclass cls, jlong peer) {
    return ((ENetPeer*)peer)->roundTripTime;
}

/*
 * Class:     logykk_enet_ENetLibrary
 * Method:    peerGetState
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetLibrary_peerGetState
  (JNIEnv *env, jclass cls, jlong peer) {
    return ((ENetPeer*)peer)->state;
}

/*
 * Class:     logykk_enet_ENetPacket
 * Method:    getData
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_logykk_enet_ENetPacket_getData
  (JNIEnv *env, jclass cls, jlong packet) {
    ENetPacket *p = (ENetPacket*)packet;
    
    if (p == NULL || p->data == NULL) {
        return NULL;
    }
    
    jbyteArray jData = (*env)->NewByteArray(env, p->dataLength);
    (*env)->SetByteArrayRegion(env, jData, 0, p->dataLength, (jbyte*)p->data);
    
    return jData;
}

/*
 * Class:     logykk_enet_ENetPacket
 * Method:    getDataLength
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetPacket_getDataLength
  (JNIEnv *env, jclass cls, jlong packet) {
    ENetPacket *p = (ENetPacket*)packet;
    
    if (p == NULL) {
        return 0;
    }
    
    return p->dataLength;
}

/*
 * Class:     logykk_enet_ENetPacket
 * Method:    getFlags
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_logykk_enet_ENetPacket_getFlags
  (JNIEnv *env, jclass cls, jlong packet) {
    ENetPacket *p = (ENetPacket*)packet;
    
    if (p == NULL) {
        return 0;
    }
    
    return p->flags;
}