package components;

import jade.GameObject;
import jade.Prefabs;
import jade.Window;

public class QuestionBlock extends Block {
    private enum BlockType {
        Coin,
        Powerup,
        Invincibility
    }

    public BlockType blockType = BlockType.Coin;

    @Override
    void playerHit(PlayerController playerController) {
        switch (blockType) {
            case Coin -> doCoin(playerController);
            case Powerup -> doPowerup(playerController);
            case Invincibility -> doInvincibility(playerController);
        }

        StateMachine stateMachine = gameObject.getComponent(StateMachine.class);
        if (stateMachine != null) {
            stateMachine.trigger("setInactive");
            setInactive();
        }
    }

    private void doCoin(PlayerController playerController) {
        GameObject coin = Prefabs.generateBlockCoin();
        coin.transform.position.set(gameObject.transform.position);
        coin.transform.position.y += 0.25f;
        Window.getScene().addGameObjectToScene(coin);
    }

    private void doPowerup(PlayerController playerController) {

    }

    private void doInvincibility(PlayerController playerController) {

    }
}
