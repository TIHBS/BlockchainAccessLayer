/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.contracts;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.6.0.
 */
public class Permissions extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5061053c806100206000396000f30060806040526004361061006c5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416631ca44d9e8114610071578063343c85b2146100da57806383c1cd8a1461013f578063857cdbb8146101d5578063a91d58b4146101f6575b600080fd5b34801561007d57600080fd5b5060408051602060046024803582810135601f81018590048502860185019096528585526100d8958335600160a060020a031695369560449491939091019190819084018382808284375094975061024f9650505050505050565b005b3480156100e657600080fd5b506100ef6102c6565b60408051602080825283518183015283519192839290830191858101910280838360005b8381101561012b578181015183820152602001610113565b505050509050019250505060405180910390f35b34801561014b57600080fd5b50610160600160a060020a0360043516610330565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561019a578181015183820152602001610182565b50505050905090810190601f1680156101c75780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156101e157600080fd5b50610160600160a060020a03600435166103e2565b34801561020257600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100d89436949293602493928401919081908401838280828437509497506104549650505050505050565b600160a060020a03821660009081526001602090815260408083203384528252909120825161028092840190610478565b5050600160a060020a031660009081526020818152604082208054600181018255908352912001805473ffffffffffffffffffffffffffffffffffffffff191633179055565b336000908152602081815260409182902080548351818402810184019094528084526060939283018282801561032557602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311610307575b505050505090505b90565b336000908152600160208181526040808420600160a060020a038616855282529283902080548451600294821615610100026000190190911693909304601f810183900483028401830190945283835260609390918301828280156103d65780601f106103ab576101008083540402835291602001916103d6565b820191906000526020600020905b8154815290600101906020018083116103b957829003601f168201915b50505050509050919050565b600160a060020a038116600090815260026020818152604092839020805484516001821615610100026000190190911693909304601f810183900483028401830190945283835260609390918301828280156103d65780601f106103ab576101008083540402835291602001916103d6565b336000908152600260209081526040909120825161047492840190610478565b5050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106104b957805160ff19168380011785556104e6565b828001600101855582156104e6579182015b828111156104e65782518255916020019190600101906104cb565b506104f29291506104f6565b5090565b61032d91905b808211156104f257600081556001016104fc5600a165627a7a72305820e7a138e875877aab46c5d9f555a0c91c68c07abb41defa3874b62eb4ee76f6100029";

    public static final String FUNC_SETPERMISSION = "setPermission";

    public static final String FUNC_GETGIVERS = "getGivers";

    public static final String FUNC_GETPERMISSION = "getPermission";

    public static final String FUNC_GETPUBLICKEY = "getPublicKey";

    public static final String FUNC_SETPUBLICKEY = "setPublicKey";

    @Deprecated
    protected Permissions(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Permissions(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Permissions(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Permissions(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> setPermission(String taker, byte[] permissionPayload) {
        final Function function = new Function(
                FUNC_SETPERMISSION, 
                Arrays.<Type>asList(new Address(taker),
                new DynamicBytes(permissionPayload)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getGivers() {
        final Function function = new Function(FUNC_GETGIVERS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<byte[]> getPermission(String giver) {
        final Function function = new Function(FUNC_GETPERMISSION, 
                Arrays.<Type>asList(new Address(giver)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<byte[]> getPublicKey(String ethereumAddress) {
        final Function function = new Function(FUNC_GETPUBLICKEY, 
                Arrays.<Type>asList(new Address(ethereumAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> setPublicKey(byte[] publicKey) {
        final Function function = new Function(
                FUNC_SETPUBLICKEY, 
                Arrays.<Type>asList(new DynamicBytes(publicKey)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<Permissions> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Permissions.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Permissions> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Permissions.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Permissions> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Permissions.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Permissions> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Permissions.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static Permissions load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Permissions(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Permissions load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Permissions(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Permissions load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Permissions(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Permissions load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Permissions(contractAddress, web3j, transactionManager, contractGasProvider);
    }
}
