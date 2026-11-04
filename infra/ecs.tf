resource "aws_ecs_express_gateway_service" "wishifai" {
  service_name            = "wishifai-7"
  execution_role_arn      = "arn:aws:iam::244530008913:role/ecsTaskExecutionRole"
  infrastructure_role_arn = "arn:aws:iam::244530008913:role/service-role/ecsInfrastructureRoleForExpressServices"
  task_role_arn           = aws_iam_role.task_role.arn
  health_check_path       = "/api/health"

  primary_container {
    image          = "244530008913.dkr.ecr.eu-west-1.amazonaws.com/wishifai:${var.image_tag}"
    container_port = 8080

    environment {
      name  = "S3_BUCKET"
      value = aws_s3_bucket.images.bucket
    }

    environment {
      name  = "AWS_REGION"
      value = var.aws_region
    }
  }

  depends_on = [
    aws_iam_role_policy.task_permissions
  ]
}