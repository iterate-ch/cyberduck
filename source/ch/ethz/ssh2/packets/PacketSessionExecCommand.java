package ch.ethz.ssh2.packets;

import java.io.UnsupportedEncodingException;
import java.io.IOException;


/**
 * PacketSessionExecCommand.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class PacketSessionExecCommand
{
	byte[] payload;

	public int recipientChannelID;
	public boolean wantReply;
	public String command;

	public PacketSessionExecCommand(int recipientChannelID, boolean wantReply, String command)
	{
		this.recipientChannelID = recipientChannelID;
		this.wantReply = wantReply;
		this.command = command;
	}
	
    public byte[] getPayload() throws IOException 
    {
        return this.getPayload(null);
    }

    public byte[] getPayload(String charsetName) throws UnsupportedEncodingException
    {
		if (payload == null)
		{
			TypesWriter tw = new TypesWriter();
			tw.writeByte(Packets.SSH_MSG_CHANNEL_REQUEST);
			tw.writeUINT32(recipientChannelID);
			tw.writeString("exec");
			tw.writeBoolean(wantReply);
			tw.writeString(command, charsetName);
			payload = tw.getBytes();
		}
		return payload;
	}
}
