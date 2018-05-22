import com.oradian.pipedream.NodeUtil
import com.oradian.pipedream.BuildUtil
import com.oradian.pipedream.integration.IntegrationConfiguration

private def call_lambda(def lambda, boolean spawned) {
    def BU = new BuildUtil(this)
    def PR = IntegrationConfiguration.getProvider(this)

    if (spawned)
        BU.cleanupWorkspace()

    try {
        def registry = env.BUILD_REGISTRY_URL
        if (!registry)
            throw new Exception("Please define BUILD_REGISTRY_URL in global Jenkins configuration")

        if (!spawned) {
            lambda(PR)
            return
        }

        docker.withRegistry(registry, 'nexus') {
            lambda(PR)
        }
    } finally {
        if (spawned)
            BU.cleanupWorkspace()
    }
}

// force will spawn a new node even if already on a node
// (but not if current node already has requested label)
def call(String label, boolean force = false, def lambda) {
    def nu = new NodeUtil(this)

    force = force && !nu.hasLabel(label)
    if (nu.isInsideNode() && !force) {
        call_lambda(lambda, false)
    } else {
        if (label == null) {
            node {
                call_lambda(lambda, true)
            }
        } else {
            node(label) {
                call_lambda(lambda, true)
            }
        }
    }
}

def call(boolean force = false, def lambda) {
    singlenode(null, force, lambda)
}
