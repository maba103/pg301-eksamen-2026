resource "aws_cloudwatch_dashboard" "wishifai" {
  dashboard_name = "Wishifai-Dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "Business - Gift Suggestions"
          region = var.aws_region
          metrics = [
            ["Wishifai", "wishifai.gift.suggestions.count"]
          ]
          period = 60
          stat   = "Sum"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "Infrastructure - ECS"
          region = var.aws_region
          metrics = [
            ["AWS/ECS", "CPUUtilization", "ClusterName", "default", "ServiceName", "wishifai-7"],
            ["AWS/ECS", "MemoryUtilization", "ClusterName", "default", "ServiceName", "wishifai-7"],
            ["AWS/ECS", "LiveTaskCount", "ClusterName", "default", "ServiceName", "wishifai-7"]
          ]
          period = 60
          stat   = "Average"
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Application - JVM Memory"
          region = var.aws_region
          metrics = [
            [
              {
                expression = "SEARCH('{Wishifai,area,id} MetricName=\"jvm.memory.used.value\"', 'Average', 60)"
                id         = "e1"
                label      = "JVM memory by pool"
              }
            ]
          ]
          period = 60
          stat   = "Average"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Application - HTTP Request Latency"
          region = var.aws_region
          metrics = [
            [
              {
                expression = "SEARCH('{Wishifai,exception,method,error,uri,outcome,status} MetricName=\"http.server.requests.avg\"', 'Average', 60)"
                id         = "e2"
                label      = "HTTP request latency"
              }
            ]
          ]
          period = 60
          stat   = "Average"
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 24
        height = 6
        properties = {
          title  = "Application - AI Service Duration"
          region = var.aws_region
          metrics = [
            ["Wishifai", "wishifai.rekognition.duration.avg"],
            ["Wishifai", "wishifai.bedrock.duration.avg"]
          ]
          period = 60
          stat   = "Average"
        }
      }
    ]
  })
}

resource "aws_cloudwatch_metric_alarm" "bedrock_latency" {
  alarm_name          = "Wishifai-Bedrock-High-Latency"
  alarm_description   = "Triggers when the maximum Bedrock request duration exceeds 30 seconds."
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "wishifai.bedrock.duration.max"
  namespace           = "Wishifai"
  period              = 60
  statistic           = "Maximum"
  threshold           = 30000
  treat_missing_data  = "notBreaching"
}
