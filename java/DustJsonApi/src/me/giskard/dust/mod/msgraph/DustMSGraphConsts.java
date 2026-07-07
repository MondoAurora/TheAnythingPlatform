package me.giskard.dust.mod.msgraph;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.dust.core.net.DustNetConsts;

public interface DustMSGraphConsts extends DustConsts, DustNetConsts, DustMindConsts {

	String MSGRAPH_value = "value";
	String MSGRAPH_userPrincipalName = "userPrincipalName";
	String MSGRAPH_mailNickname = "mailNickname";
	String MSGRAPH_mail = "mail";
	
	
	
	String TOKEN_MSGRAPH_AUTHORITY = UNIT_DUST + DUST_SEP_TOKEN + "msgraphAuthority";
	String TOKEN_MSGRAPH_CLIENTID = UNIT_DUST + DUST_SEP_TOKEN + "msgraphClientId";
	String TOKEN_MSGRAPH_SECRET = UNIT_DUST + DUST_SEP_TOKEN + "msgraphSecret";
	String TOKEN_MSGRAPH_SCOPE = UNIT_DUST + DUST_SEP_TOKEN + "msgraphScope";

	String TOKEN_MSGRAPH_REQUEST = UNIT_DUST + DUST_SEP_TOKEN + "msgraphRequest";

}
