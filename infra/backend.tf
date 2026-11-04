terraform {
  backend "s3" {
    bucket = "pgr301-terraform-state"
    key    = "wishifai/7/terraform.tfstate"
    region = "eu-west-1"
  }
}