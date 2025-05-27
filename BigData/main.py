""" 전체 데이터 수집/병합/저장 과정을 통합 실행"""
from fetch import BidDataFetcher
from collect import BidDetailCollector
from join import BidDataJoiner
from datetime import datetime, timedelta

class BidDataPipeLine:
    def __init__(self, start_date=None, end_date=None):
        if not start_date or not end_date:
            today = datetime.now()
            yesterday = today - timedelta(days=1)
            self.start_date = yesterday.strftime("%Y%m%d") + "0000"
            self.end_date = today.strftime("%Y%m%d") + "0000"
        else:
            self.start_date = start_date
            self.end_date = end_date


    def run(self):
        # Step 1: 낙찰 공고 데이터 수집 
        fetcher = BidDataFetcher(self.start_date, self.end_date)
        fetcher.fetch_all()

        # Step 2: 추가 API 요청
        collector = BidDetailCollector()
        collector.collect_all()

        # Step 3: 병합
        joiner = BidDataJoiner()
        joiner.run()

if __name__ == "__main__":
    pipeline = BidDataPipeLine()
    pipeline.run()
