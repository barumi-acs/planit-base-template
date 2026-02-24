/**
 * [planit 글로벌 룰 - grpc 테스트 컨트롤러]
 * 서비스 실행 테스트 및 예시를 보여줍니다.
 * 삭제하셔도 됩니다.
 * @since 2026-02-24
 */

package com.planit.basetemplate.grpc;

import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
@GrpcService
public class BaseGrpcController extends BaseTestGrpcServiceGrpc.BaseTestGrpcServiceImplBase {

    @Override
    public void ping(PingRequest request, StreamObserver<PongResponse> responseObserver) {
        log.info("gRPC Ping 요청 수신: {}", request.getMessage());

        PongResponse response = PongResponse.newBuilder()
                .setReply("Pong: " + request.getMessage())
                .setTraceId(UUID.randomUUID().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}