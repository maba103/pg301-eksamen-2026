resource "aws_s3_bucket" "images" {
  bucket = "wishifai-images-${var.candidate_number}-244530008913"

  tags = {
    Name = "Wishifai image storage"
  }
}

resource "aws_s3_bucket_public_access_block" "images" {
  bucket = aws_s3_bucket.images.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}