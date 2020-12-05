package pl.edu.pw.ddm.platform.core.instance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ManualInstanceSetupValidator {

    private final InstanceFacade.SetupRequest setup;

    void validate() {
        // TODO add another model
        checkState("central".equals(setup.getDdmModel()), "Non central ddmModel: '%s'", setup.getDdmModel());

        // FIXME for central at least 1 master and 1 worker
        checkState(setup.getNodes().size() > 1, "Less than 2 nodes in setup (%s<2)", setup.getNodes().size());
        checkNotNull(masterNode(), "Provide exactly 1 master node");

        var masterNode = masterNode();
        var workerNodes = workerNodes();
        checkState(workerNodes.size() + 1 == setup.getNodes().size(), "provided not master nor worker node type or more than 1 master node");

        verifyNode(masterNode);
        workerNodes.forEach(this::verifyNode);
    }

    InstanceConfig.InstanceData toInstanceData() {
        var nodes = setup.getNodes()
                .stream()
                .map(n -> new InstanceConfig.InstanceNode(
                        IdGenerator.nodeId(n.getName(), null),
                        null,
                        n.getName(),
                        n.getType(),
                        n.getAddress(),
                        null,
                        null,
                        false,
                        n.getPort(),
                        n.getUiPort(),
                        n.getAgentPort(),
                        n.getCpu(),
                        n.getMemoryInGb(),
                        null)
                ).collect(Collectors.toMap(InstanceConfig.InstanceNode::getId, n -> n));

        return new InstanceConfig.InstanceData(
                IdGenerator.instanceId(),
                InstanceConfig.InstanceType.MANUAL_SETUP,
                null,
                nodes,
                new InstanceConfig.InstanceInfo()
        );
    }

    private void verifyNode(InstanceFacade.SetupRequest.ManualSetupNode node) {
        checkState(Strings.nullToEmpty(node.getName()).length() <= 32, "Name '%s' length should be less than 32 (%s>32)", node.getName(), Strings.nullToEmpty(node.getName()).length());
        // FIXME telnet address:port
        // FIXME telnet address:uiPort
        // FIXME telnet address:agentPort
        checkState(node.getCpu() > 1, "Cpu must be positive value (%s<1)", node.getCpu());
        checkState(node.getMemoryInGb() >= 1, "Setup more than 1Gb per node (%s<1)", node.getMemoryInGb());
    }

    private InstanceFacade.SetupRequest.ManualSetupNode masterNode() {
        return setup.getNodes()
                .stream()
                .filter(n -> "master".equals(n.getType()))
                .findFirst()
                .orElse(null);
    }

    private List<InstanceFacade.SetupRequest.ManualSetupNode> workerNodes() {
        return setup.getNodes()
                .stream()
                .filter(n -> "worker".equals(n.getType()))
                .collect(Collectors.toUnmodifiableList());
    }

}
