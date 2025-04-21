package tbank.ab.controller

import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.ChatEndpoints
import tbank.ab.service.ChatService

private class ChatController[F[_]](
  chatService: ChatService[F]
) extends Controller[F] {

  private val animalChat: ServerEndpoint[Fs2Streams[F] & WebSockets, F] =
    ChatEndpoints
      .animalChat
      .serverLogicSuccess(chatService.chat)

  override val endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]] =
    List(animalChat)
      .map(_.withTag("Chat"))
}

object ChatController {
  def make[F[_]](using chatService: ChatService[F]): Controller[F] =
    new ChatController(chatService)
}
