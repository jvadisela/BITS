package com.bits.dc;

import com.bits.dc.utils.InputUtil;
import com.bits.dc.utils.StorageUtil;

public class Client extends AbstractMachine {

	/**
	 * Description: method name,node host,node id,existing node host,existing
	 * node id Example: join,localhost,10,none,0 Example:
	 * join,localhost,15,localhost,10 Example: join,localhost,20,localhost,15
	 * Example: join,localhost,25,localhost,20 Example:
	 * join,localhost,20,localhost,25 Example: view Example: cut
	 */
	public static void main(String[] args) {
		System.out.println(
				"You can change service configuration parameters in " + ServiceConfiguration.CONFIGURATION_FILE);
		System.out.println("Service configuration: RMI port=" + RMI_PORT);
		System.out.println("Service configuration: BankTransfer MIN_AMOUNT=" + Constants.MIN_AMOUNT + ", MAX_AMOUNT="
				+ Constants.MAX_AMOUNT + ", INITIAL_BALANCE=" + Constants.INITIAL_BALANCE);
		System.out.println("Service configuration: BankTransfer TIMEOUT_FREQUENCY=" + Constants.TIMEOUT_FREQUENCY
				+ ", TIMEOUT_UNIT=" + Constants.TIMEOUT_UNIT);
		if (Constants.MIN_AMOUNT >= Constants.MAX_AMOUNT || Constants.MAX_AMOUNT >= Constants.INITIAL_BALANCE) {
			System.out.println(
					"Bank transfer properties must maintain formula [ MIN_AMOUNT < MAX_AMOUNT < INITIAL_BALANCE ] !");
			return;
		}
		System.out.println("Type in: method name,node host,node id,existing node host,existing node id");
		System.out.println("Example: create,localhost,10");
		System.out.println("Example: join,localhost,15,localhost,10");
		System.out.println("Example: join,localhost,20,localhost,15");
		System.out.println("Example: join,localhost,25,localhost,20");
		System.out.println("Example: join,localhost,30,localhost,25");
		System.out.println("Example: view");
		System.out.println("Example: cut");
		StorageUtil.init();
		System.out.println("Bank is ready for request >");
		InputUtil.readInput(ServerLauncher.class.getName());
	}

}
