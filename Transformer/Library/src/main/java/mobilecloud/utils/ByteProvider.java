package mobilecloud.utils;

/**
 * An abstract provider that can provide bytes
 */
public interface ByteProvider {
    
    /**
     * Provide the bytes data
     * @return the bytes data
     */
    public byte[] provide();
    
}
