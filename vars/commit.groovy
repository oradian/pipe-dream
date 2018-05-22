import com.oradian.pipedream.docker.Docker

// To be called inside of Docker container
def call(String image) {
    return Docker.create(this).commit(image)
}

def call(String containerId, String image) {
    return Docker.create(this).commit(containerId, image)
}
