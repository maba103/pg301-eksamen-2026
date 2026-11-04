variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}

variable "candidate_number" {
  description = "Candidate number"
  type        = string
}

variable "image_tag" {
  description = "Docker image tag"
  type        = string
}