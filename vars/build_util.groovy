def forwardParameter(String parameterName) {
    if (!env[parameterName]) {
        throw new Exception("Can't forward parameter '${parameterName}': no such parameter defined")
    }

    def parameter = params[parameterName]
    switch (parameter.class) {
        case String:
            return new StringParameterValue(parameterName, parameter)

        default:
            throw new Exception("forwardParameter of type ${parameter.class} not implemented! Please implement it in ${this.class.name}")
    }
}
