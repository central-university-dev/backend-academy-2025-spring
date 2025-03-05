package tbank.ab.controller

import cats.effect.IO
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.ChatEndpoints
import tbank.ab.service.ChatService

private class ChatController(
  chatService: ChatService[IO]
) extends Controller[IO] {

  private val animalChat: ServerEndpoint[Fs2Streams[IO] & WebSockets, IO] =
    ChatEndpoints
      .animalChat
      .serverLogicSuccess(chatService.chat)

  override val endpoints: List[ServerEndpoint[Fs2Streams[IO] & WebSockets, IO]] =
    List(animalChat)
      .map(_.withTag("Chat"))
}

object ChatController {
  def make(chatService: ChatService[IO]): Controller[IO] =
    new ChatController(chatService)
}
