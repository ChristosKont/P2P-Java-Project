import java.io.Serializable;

public class connectedPeer implements Serializable {

    private int tokenId;
    private String ipAddress;
    private int port;
    private String username;
    private int countDownloads;
    private int countFailures;

    public connectedPeer (int tokenId, String ipAddress, int port, String username) {
        this.tokenId = tokenId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = username;
        this.countDownloads = 0;
        this.countFailures = 0;
    }

    public int getTokenId() { return tokenId; }
    public void setTokenId(int tokenId) { this.tokenId = tokenId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getCountDownloads() { return countDownloads; }
    public void setCountDownloads(int countDownloads) { this.countDownloads = countDownloads; }

    public int getCountFailures() { return countFailures; }
    public void setCountFailures(int countFailures) { this.countFailures = countFailures; }

    @Override
    public String toString () {

        return  "IP Address = " + getIpAddress() +
                "\nPort = " + getPort() +
                "\nUsername = " + getUsername() +
                "\nCount of Downloads = " + getCountDownloads() +
                "\nCount of Failures = " + getCountFailures();
    }

}
