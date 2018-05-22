def call(lambda) {
	if (!env.TIMESTAMPS_IS_SET) {
		env.TIMESTAMPS_IS_SET = true
		timestamps {
			lambda()
		}
		env.TIMESTAMPS_IS_SET = false
	} else {
		lambda()
	}
}
